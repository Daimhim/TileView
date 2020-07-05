package com.moagrius;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.internal.jni.CoreMap;
import com.esri.arcgisruntime.layers.WmtsLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.ogc.wmts.WmtsLayerInfo;
import com.esri.arcgisruntime.ogc.wmts.WmtsService;
import com.esri.arcgisruntime.ogc.wmts.WmtsServiceInfo;

import java.util.List;

public class TileViewDemoXmlActivity extends AppCompatActivity {
    private MapView mMapView;
//    private String wmts_url = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/WorldTimeZones/MapServer/WMTS";
    private String wmts_url = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_view_demo_xml);
        mMapView = findViewById(R.id.map_view);
        final ArcGISMap map = new ArcGISMap();
        map.setBasemap(Basemap.createNavigationVector());
        mMapView.setMap(map);
        // create wmts service from url string
        WmtsService wmtsService = new WmtsService(wmts_url);
        wmtsService.addDoneLoadingListener(() -> {
            Log.e("xyh", wmtsService.getLoadStatus().name());
            if (wmtsService.getLoadStatus() == LoadStatus.LOADED) {
                // get service info
                WmtsServiceInfo wmtsServiceInfo = wmtsService.getServiceInfo();
                // get the first layer id
                List<WmtsLayerInfo> layerInfoList = wmtsServiceInfo.getLayerInfos();
                WmtsLayerInfo lWmtsLayerInfo = null;
                for (int i = 0; i < layerInfoList.size(); i++) {
                    lWmtsLayerInfo = layerInfoList.get(i);
                    if (lWmtsLayerInfo.getTitle().startsWith("beijing_mapabc_200702142108")){
                        map.getBasemap().getBaseLayers().add(new WmtsLayer(lWmtsLayerInfo));
                    }else {
                        map.getOperationalLayers().add(new WmtsLayer(lWmtsLayerInfo));
                    }
                }

            } else {
                wmtsService.getLoadError().printStackTrace();

                String error = "Error loading WMTS Service: " + wmtsService.getLoadError().getMessage() + wmtsService.getLoadError().getErrorCode();
                Log.e("xyh", error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
        wmtsService.loadAsync();
    }
    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        mMapView.dispose();
        super.onDestroy();
    }
}
