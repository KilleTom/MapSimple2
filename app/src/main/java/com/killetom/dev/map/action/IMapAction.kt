package com.killetom.dev.map.action

import com.killetom.dev.map.data.BaseLocation

interface IMapAction {

    fun mapReadychecker()

    fun initMap()

    fun setCenter(lat:Double,lng:Double) = setCenter(BaseLocation(lat, lng))

    fun getPresionLocation():BaseLocation

    fun setCenter(location: BaseLocation)

    fun getMapCenter():BaseLocation

    fun showCurrentLocation()

    fun navDiverLocation(lat: Double,lng: Double)=navDiverLocation(BaseLocation(lat, lng))
    fun navDiverLocation(location: BaseLocation)

    fun navWalkLocation(lat: Double,lng: Double)=navWalkLocation(BaseLocation(lat, lng))
    fun navWalkLocation(location: BaseLocation)

    fun navRiderLocation(lat: Double,lng: Double)=navRiderLocation(BaseLocation(lat, lng))
    fun navRiderLocation(location: BaseLocation)


    fun startSelectMapNavLocation()

    fun startSelectMapNavLocationFormEmulator()

}