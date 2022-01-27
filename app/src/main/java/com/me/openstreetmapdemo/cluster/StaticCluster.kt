package com.me.openstreetmapdemo.cluster

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.util.*

open class StaticCluster(var mCenter: GeoPoint) {

    protected val mItems = ArrayList<Marker>()

    protected var mMarker: Marker? = null

    open fun setPosition(center: GeoPoint) {
        mCenter = center
    }

    open fun getPosition(): GeoPoint? {
        return mCenter
    }

    open fun getSize(): Int {
        return mItems.size
    }

    open fun getItem(index: Int): Marker? {
        return mItems[index]
    }

    open fun add(t: Marker): Boolean {
        return mItems.add(t)
    }

    /** set the Marker to be displayed for this cluster  */
    open fun setMarker(marker: Marker) {
        mMarker = marker
    }

    /** @return the Marker to be displayed for this cluster
     */
    open fun getMarker(): Marker? {
        return mMarker
    }

    open fun getBoundingBox(): BoundingBox? {
        if (getSize() == 0) return null
        var p = getItem(0)!!.position
        val bb = BoundingBox(p.latitude, p.longitude, p.latitude, p.longitude)
        for (i in 1 until getSize()) {
            p = getItem(i)!!.position
            val minLat = bb.latSouth.coerceAtMost(p.latitude)
            val minLon = bb.lonWest.coerceAtMost(p.longitude)
            val maxLat = bb.latNorth.coerceAtLeast(p.latitude)
            val maxLon = bb.lonEast.coerceAtLeast(p.longitude)
            bb[maxLat, maxLon, minLat] = minLon
        }
        return bb
    }
}