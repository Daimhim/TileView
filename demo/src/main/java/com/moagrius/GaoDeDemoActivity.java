package com.moagrius;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Tile;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GaoDeDemoActivity extends AppCompatActivity {
    private MapView mMapView;
    private AMap aMap;
    private TileOverlay mtileOverlay;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gao_de_demo);
        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //移动中心点到故宫
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.918255, 116.397369), 15));
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        //我们可以在这里进行离线瓦片的加载(在线瓦片和离线瓦片一样只不过一个是本地瓦片存储路径，一个是网络Url)
        loadOffLineMap();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);//保存地图当前的状态
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    /**
     * 加载离线线瓦片数据
     */
    private void loadOffLineMap() {
        final String url = "/storage/emulated/0/amap/tilecache";
        TileOverlayOptions tileOverlayOptions =
                new TileOverlayOptions().tileProvider(new LocalTileProvider(url));
        tileOverlayOptions.diskCacheEnabled(true)
                .diskCacheDir("/storage/emulated/0/amap/OMCcache")
                .diskCacheSize(100000)
                .memoryCacheEnabled(true)
                .memCacheSize(100000)
                .zIndex(-9999);
        mtileOverlay = aMap.addTileOverlay(tileOverlayOptions);
    }

    //实现TitlProvider 用于加载本瓦片
    class LocalTileProvider implements TileProvider {

        private static final int TILE_WIDTH = 256;
        private static final int TILE_HEIGHT = 256;
        public static final int BUFFER_SIZE = 16 * 1024;
        private String tilePath;

        public LocalTileProvider(String path) {
            tilePath=path;
        }

        @Override
        public Tile getTile(int x, int y, int zoom) {
            byte[] image = readTileImage(x, y, zoom);
            return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        }

        private byte[] readTileImage(int x, int y, int zoom) {
            InputStream in = null;
            ByteArrayOutputStream buffer = null;
            File f = new File(getTileFilename(x, y, zoom));
            if(f.exists()){
                try {
                    buffer = new ByteArrayOutputStream();
                    in = new FileInputStream(f);
                    int nRead;
                    byte[] data = new byte[BUFFER_SIZE];
                    while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    return buffer.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    if (buffer != null)
                        try {
                            buffer.close();
                        } catch (Exception e) {
                        }
                }
            }else{
                return null;
            }
        }

        private String getTileFilename(int x, int y, int zoom) {
            //这里一定要注意，由于瓦片都是由专门的切图工具生成，这个x,y指的是切片工具根据该瓦片在地图上中心点经纬度的
//            所生成的固定的值，所以瓦片图片的名字千万不要随意改动，而这个z值则是当前地图缩放级别，当你缩放地图时高德
//                    地图就会不停的监控地图级别然后在指定的本地路径查找对应的瓦片进行加载
            return tilePath + "/" + x + "_" + y + "_" + zoom + ".png";
        }

        @Override
        public int getTileWidth() {
            return TILE_HEIGHT;
        }

        @Override
        public int getTileHeight() {
            return TILE_WIDTH;
        }
    }
}
