package com.me.openstreetmapdemo

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.me.openstreetmapdemo.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var mapView : MapView? = null

    private var mMapController:MapController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        mapView = MapView(this)
        binding.flContainer.addView(mapView,ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        mapView?.apply {
            //设置地图源
            setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            mMapController = mapView?.controller as MapController
            //是否显示缩放按钮
            setBuiltInZoomControls(false)
            //是否能触控
            setMultiTouchControls(true)
            //设置旋转
            val gestureOverlay = RotationGestureOverlay(this)
            gestureOverlay.isEnabled = true
            overlays.add(gestureOverlay)
        }

        mMapController?.apply {
            setZoom(16)
            animateTo(GeoPoint(39.901873,116.32665))
        }

    }
}