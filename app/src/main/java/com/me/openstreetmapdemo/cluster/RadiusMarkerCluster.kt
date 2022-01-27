package com.me.openstreetmapdemo.cluster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import com.me.openstreetmapdemo.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*
import kotlin.math.sqrt

class RadiusMarkerCluster(val ctx: Context) : MarkerCluster() {

    private var mMaxClusteringZoomLevel = 17
    private var mRadiusInPixels = 100
    private var mRadiusInMeters = 0.0
    private var mTextPaint: Paint? = null
    private var mClonedMarkers: ArrayList<Marker>? = null
    private var mAnimated = false
    var mDensityDpi = 0

    /** cluster icon anchor  */
    var mAnchorU = Marker.ANCHOR_CENTER

    /** cluster icon anchor  */
    var mAnchorV = Marker.ANCHOR_CENTER

    /** anchor point to draw the number of markers inside the cluster icon  */
    var mTextAnchorU = Marker.ANCHOR_CENTER

    /** anchor point to draw the number of markers inside the cluster icon  */
    var mTextAnchorV = Marker.ANCHOR_CENTER

    init {
        mTextPaint = Paint()
        mTextPaint?.color = Color.WHITE
        mTextPaint?.textSize = 15 * ctx.resources.displayMetrics.density
        mTextPaint?.isFakeBoldText = true
        mTextPaint?.textAlign = Paint.Align.CENTER
        mTextPaint?.isAntiAlias = true
        val clusterIconD = ctx.resources.getDrawable(R.drawable.marker_cluster)
        val clusterIcon = (clusterIconD as BitmapDrawable).bitmap
        setIcon(clusterIcon)
        mAnimated = true
        mDensityDpi = ctx.resources.displayMetrics.densityDpi
    }

    /** If you want to change the default text paint (color, size, font)  */
    fun getTextPaint(): Paint? {
        return mTextPaint
    }

    /** Set the radius of clustering in pixels. Default is 100px.  */
    fun setRadius(radius: Int) {
        mRadiusInPixels = radius
    }

    /** Set max zoom level with clustering. When zoom is higher or equal to this level, clustering is disabled.
     * You can put a high value to disable this feature.  */
    fun setMaxClusteringZoomLevel(zoom: Int) {
        mMaxClusteringZoomLevel = zoom
    }

    override fun cluster(mapView: MapView): ArrayList<StaticCluster> {
        val clusters = ArrayList<StaticCluster>()
        convertRadiusToMeters(mapView)

        mClonedMarkers = ArrayList(mItems) //shallow copy

        while (mClonedMarkers!!.isNotEmpty()) {
            val m = mClonedMarkers!![0]
            val cluster: StaticCluster = createCluster(m, mapView)
            clusters.add(cluster)
        }
        return clusters
    }

    override fun buildClusterMarker(cluster: StaticCluster, mapView: MapView): Marker {
        val m = Marker(mapView)
        m.position = cluster.getPosition()
        m.setInfoWindow(null)
        m.setAnchor(mAnchorU, mAnchorV)

        val finalIcon = Bitmap.createBitmap(mClusterIcon!!.getScaledWidth(mDensityDpi),
                mClusterIcon!!.getScaledHeight(mDensityDpi), mClusterIcon!!.config)
        val iconCanvas = Canvas(finalIcon)
        iconCanvas.drawBitmap(mClusterIcon!!, 0f, 0f, null)
        var text = "" + cluster.getSize()
        if (cluster.getSize() > 1000) {
            text = "999+"
        }
        val textHeight = (mTextPaint!!.descent() + mTextPaint!!.ascent()).toInt()
        iconCanvas.drawText(text,
                mTextAnchorU * finalIcon.width,
                mTextAnchorV * finalIcon.height - textHeight / 2,
                mTextPaint!!)
        m.icon = BitmapDrawable(mapView.context.resources, finalIcon)

        return m
    }

    override fun renderer(clusters: ArrayList<StaticCluster>, canvas: Canvas, mapView: MapView) {
        for (cluster in clusters) {
            if (cluster.getSize() === 1) {
                //cluster has only 1 marker => use it as it is:
                cluster.setMarker(cluster.getItem(0)!!)
            } else {
                //only draw 1 Marker at Cluster center, displaying number of Markers contained
                val m = buildClusterMarker(cluster, mapView)
                cluster.setMarker(m)
            }
        }
    }

    private fun convertRadiusToMeters(mapView: MapView) {
        val mScreenRect = mapView.getIntrinsicScreenRect(null)
        val screenWidth = mScreenRect.right - mScreenRect.left
        val screenHeight = mScreenRect.bottom - mScreenRect.top
        val bb = mapView.boundingBox
        val diagonalInMeters = bb.diagonalLengthInMeters
        val diagonalInPixels = sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble())
        val metersInPixel = diagonalInMeters / diagonalInPixels
        mRadiusInMeters = mRadiusInPixels * metersInPixel
    }

    private fun createCluster(m: Marker, mapView: MapView): StaticCluster {
        val clusterPosition = m.position
        val cluster = StaticCluster(clusterPosition)
        cluster.add(m)
        mClonedMarkers!!.remove(m)
        if (mapView.zoomLevel > mMaxClusteringZoomLevel) {
            //above max level => block clustering:
            return cluster
        }
        val it = mClonedMarkers!!.iterator()
        while (it.hasNext()) {
            val neighbour = it.next()
            val distance = clusterPosition.distanceToAsDouble(neighbour.position)
            if (distance <= mRadiusInMeters) {
                cluster.add(neighbour)
                it.remove()
            }
        }
        return cluster
    }

    fun setAnimation(animate: Boolean) {
        mAnimated = animate
    }

    override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
        for (cluster in reversedClusters()) {
            if (cluster.getMarker()!!.onSingleTapConfirmed(event, mapView)) {
                if (mAnimated && cluster.getSize() > 1) zoomOnCluster(mapView, cluster)
                return true
            }
        }
        return false
    }

    private fun zoomOnCluster(mapView: MapView, cluster: StaticCluster) {
        var bb = cluster.getBoundingBox()
        if (bb!!.latNorth != bb.latSouth || bb.lonEast != bb.lonWest) {
            bb = bb.increaseByScale(1.15f)
            mapView.zoomToBoundingBox(bb, true)
        } else  //all points exactly at the same place:
            mapView.setExpectedCenter(bb.centerWithDateLine)
    }
}