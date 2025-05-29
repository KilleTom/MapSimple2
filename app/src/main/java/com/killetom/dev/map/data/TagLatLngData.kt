package com.killetom.dev.map.data

class TagLatLngData {


    var name:String = ""

    var poiID :String?=""

    var city: String = ""

    var latLng: BaseLocation = BaseLocation(0.0, 0.0)

    var address:String = ""

    var disdanceMessage:String = ""


    class TagPhoto {
        var title: String = ""
        var photoUrl: String = ""
    }
}