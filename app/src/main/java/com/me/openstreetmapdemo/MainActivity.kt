package com.me.openstreetmapdemo

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var mapView: MapView? = null

    private var mMapController: MapController? = null

    private var pointList = mutableListOf<GeoPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        mapView = MapView(this)
        binding.flContainer.addView(mapView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        mapView?.apply {
            //设置地图源
//            setTileSource(TileSourceFactory.USGS_SAT)
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
        binding.btnPolygon.setOnClickListener {
            if (pointList.isNotEmpty()) {
                addPolygon(pointList)
            }
        }

        binding.btnPolyline.setOnClickListener {
            if (pointList.isNotEmpty()) {
                addPolyline(pointList)
            }
        }

        binding.btnClear.setOnClickListener {
            mapView?.overlayManager?.clear()
            pointList.clear()
            mapView?.overlays?.add(0, MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    pointList.add(p)
                    addMarker(p)
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean {
                    return true
                }
            }))
            mapView?.invalidate()
        }

        mapView?.apply {
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    return false
                }
            })


            overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    pointList.add(p)
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
        Log.d("addMarker>>>", p.toString())
        val marker = Marker(mapView)
        marker.position = p
//        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.isDraggable = true
        marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.loation_end, null)
        marker.setAnchor(0.5f, 0.5f)

//        marker.setInfoWindow()
        marker.setOnMarkerClickListener { markers, _ ->
            Toast.makeText(this, markers.position.toString(), Toast.LENGTH_SHORT).show()
            true
        }
        mapView?.overlays?.add(marker)
        mapView?.invalidate()
        Log.d("addMarker1>>>", marker.position.toString())
    }

    private fun addPolygon(pointList: List<GeoPoint>) {
        Log.d("addMarker2>>>", pointList.toString())
        val po = Polygon()
        po.strokeColor = Color.BLACK
        po.strokeWidth = 2f
        po.fillPaint.color = Color.BLUE
        po.points = pointList
        po.actualPoints
        mapView?.overlayManager?.add(po)
        mapView?.invalidate()
    }

    private fun addPolyline(pointList: List<GeoPoint>) {
        val polyline = Polyline(mapView, false, false)
        val paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
            strokeWidth = 10f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        polyline.outlinePaintLists.add(MonochromaticPaintList(paint))
        polyline.setPoints(pointList)
        mapView?.overlayManager?.add(polyline)
        mapView?.invalidate()
    }


}