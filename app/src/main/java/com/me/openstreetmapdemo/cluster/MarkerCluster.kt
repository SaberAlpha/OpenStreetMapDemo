package com.me.openstreetmapdemo.cluster

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.util.*

abstract class MarkerCluster : Overlay() {

    protected val FORCE_CLUSTERING = -1
    protected var mItems = ArrayList<Marker>()
    protected var mLastZoomLevel = 0
    protected var mClusterIcon: Bitmap? = null
    protected var mName: String? = null
    protected var mDescription: String? = null
    protected var mClusters = ArrayList<StaticCluster>()


    /** clustering algorithm  */
    abstract fun cluster(mapView: MapView): ArrayList<StaticCluster>

    /** Build the marker for a cluster.  */
    abstract fun buildClusterMarker(cluster: StaticCluster, mapView: MapView): Marker

    /** build clusters markers to be used at next draw  */
    abstract fun renderer(clusters: ArrayList<StaticCluster>, canvas: Canvas, mapView: MapView)

    init {
        mLastZoomLevel = FORCE_CLUSTERING
    }

    open fun setName(name: String) {
        mName = name
    }

    open fun getName(): String? {
        return mName
    }

    open fun setDescription(description: String) {
        mDescription = description
    }

    open fun getDescription(): String? {
        return mDescription
    }

    /** Set the cluster icon to be drawn when a cluster contains more than 1 marker.
     * If not set, default will be the default osmdroid marker icon (which is really inappropriate as a cluster icon).  */
    open fun setIcon(icon: Bitmap) {
        mClusterIcon = icon
    }

    /** Add the Marker.
     * Important: Markers added in a MarkerClusterer should not be added in the map overlays.  */
    open fun add(marker: Marker?) {
        mItems.add(marker!!)
    }

    /** Force a rebuild of clusters at next draw, even without a zooming action.
     * Should be done when you changed the content of a MarkerClusterer.  */
    open fun invalidate() {
        mLastZoomLevel = FORCE_CLUSTERING
    }

    /** @return the Marker at id (starting at 0)
     */
    open fun getItem(id: Int): Marker {
        return mItems[id]
    }

    /** @return the list of Markers.
     */
    open fun getItems(): ArrayList<Marker> {
        return mItems
    }

    protected open fun hideInfoWindows() {
        for (m in mItems) {
            if (m.isInfoWindowShown) m.closeInfoWindow()
        }
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
//        super.draw(canvas, mapView, shadow)
        if (shadow) return
        //if zoom has changed and mapView is now stable, rebuild clusters:
        val zoomLevel = mapView.zoomLevel
        if (zoomLevel != mLastZoomLevel && !mapView.isAnimating) {
            hideInfoWindows()
            mClusters = cluster(mapView)
            renderer(mClusters, canvas, mapView)
            mLastZoomLevel = zoomLevel
        }

        for (cluster in mClusters) {
            cluster.getMarker()?.draw(canvas, mapView.projection)
        }
    }

    open fun reversedClusters(): Iterable<StaticCluster> {
        return object : Iterable<StaticCluster> {
            override fun iterator(): Iterator<StaticCluster> {
                val i = mClusters.listIterator(mClusters.size)
                return object : MutableIterator<StaticCluster> {
                    override fun hasNext(): Boolean {
                        return i.hasPrevious()
                    }

                    override fun next(): StaticCluster {
                        return i.previous()
                    }

                    override fun remove() {
                        i.remove()
                    }
                }
            }
        }
    }

    override fun onSingleTapUp(event: MotionEvent, mapView: MapView): Boolean {
        for (cluster in reversedClusters()) {
            if (cluster.getMarker()!!.onSingleTapConfirmed(event, mapView)) return true
        }
        return false
    }

    override fun onLongPress(event: MotionEvent, mapView: MapView): Boolean {
        for (cluster in reversedClusters()) {
            if (cluster.getMarker()!!.onLongPress(event, mapView)) return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent, mapView: MapView): Boolean {
        for (cluster in reversedClusters()) {
            if (cluster.getMarker()!!.onTouchEvent(event, mapView)) return true
        }
        return false
    }

    override fun getBounds(): BoundingBox? {
        if (mItems.size == 0) return null
        var minLat = Double.MAX_VALUE
        var minLon = Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE
        var maxLon = -Double.MAX_VALUE
        for (item in mItems) {
            val latitude = item.position.latitude
            val longitude = item.position.longitude
            minLat = minLat.coerceAtMost(latitude)
            minLon = minLon.coerceAtMost(longitude)
            maxLat = maxLat.coerceAtLeast(latitude)
            maxLon = maxLon.coerceAtLeast(longitude)
        }
        return BoundingBox(maxLat, maxLon, minLat, minLon)
    }
}