package com.me.openstreetmapdemo

import org.osmdroid.util.GeoPoint
import java.lang.Math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Utils {


    fun encrypt(wgsLat: Double, wgsLon: Double): GeoPoint {
        var dwgsLat: Double
        var dwgsLon: Double
        if (outOfChina(wgsLat, wgsLon)) {
            dwgsLat = wgsLat
            dwgsLon = wgsLon
        }
        val p = delta(wgsLat, wgsLon)
        dwgsLat = wgsLat + p.altitude
        dwgsLon = wgsLon + p.longitude
        return GeoPoint(dwgsLat, dwgsLon)
    }

    fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347) return true
        return lat < 0.8293 || lat > 55.8271
    }

    fun delta(lat: Double, lon: Double): GeoPoint {
        val a = 6378245.0 // a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
        val ee = 0.00669342162296594323f // ee: 椭球的偏心率。
        var dLat = transformLat(lon - 105.0, lat - 35.0)
        var dLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat = (lat / 180.0 * PI)
        var magic = sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * PI)
        dLon = dLon * 180.0 / (a / sqrtMagic * cos(radLat) * PI)
        return GeoPoint(dLat, dLon)
    }

    fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + (0.1
                * sqrt(abs(x)))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0
                * PI)) * 2.0 / 3.0
        return ret
    }
}