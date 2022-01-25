package com.me.openstreetmapdemo

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.me.openstreetmapdemo.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
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
        binding.flContainer.addView(mapView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
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
            animateTo(GeoPoint(39.901873, 116.32665))
        }

        initListener()

    }

    private fun initListener() {
        mapView?.apply {
            addMapListener(object :MapListener{
                override fun onScroll(event: ScrollEvent?): Boolean {
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    return false
                }
            })


            overlays.add(0,MapEventsOverlay(object :MapEventsReceiver{
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    addMarker(p)
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    return true
                }
            }))
        }
    }

    private fun addMarker(p: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = p
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.isDraggable = true
        marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.loation_end, null)
        marker.setOnMarkerClickListener { markers, _ ->
            Toast.makeText(this, markers.position.toString(), Toast.LENGTH_SHORT).show()
            true
        }
        mapView?.overlays?.add(marker)
    }
}