package com.killetom.dev.map.data

open class BaseLocation {

    private var _lat:Double = 0.0

    private var _lng:Double = 0.0

    private var _alt:Double = 0.0

    constructor (lat: Double,lng: Double){
        setLocation(lat, lng)
    }

    constructor (lat: Double,lng: Double,alt: Double){
        setLocation(lat, lng,alt)
    }

    fun setLocation(lat:Double,lng:Double){
        _lat = lat
        _lng = lng
    }

    fun setLocation(lat:Double,lng:Double,alt: Double){
        _lat = lat
        _lng = lng
        _alt = alt
    }



    fun getLat() = _lat

    fun getLng() = _lng

    fun getAlt() = _alt
}