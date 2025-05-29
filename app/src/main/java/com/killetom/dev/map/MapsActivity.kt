package com.killetom.dev.map


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory
import androidx.lifecycle.observe
import com.amap.api.maps.AMap
import com.killetom.dev.map.databinding.ActivityMapsBinding
import com.killetom.dev.map.vm.InstanceMapActionVM
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class MapsActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMapsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application: MapSimpleApp = this.application as MapSimpleApp
        val mapActionVM: InstanceMapActionVM =
            ViewModelProvider(
                application.getGlobalViewModelStore(),
                NewInstanceFactory()
            )[InstanceMapActionVM::class.java]

        var iMapFragment: IMapFragment = AMapFragment(mapActionVM)

        supportFragmentManager.beginTransaction().add(binding.mapView.id, iMapFragment).commitNow()

        binding.navButton.isEnabled = false

        mapActionVM.navReadyStatus.observe(this) {
            binding.navButton.isEnabled = it
            binding.navEmualButton.isEnabled = it
        }

        mapActionVM.navPlaneStatus.observe(this) {
            binding.calculateRiderButton.isEnabled = it
            binding.calculateWalkButton.isEnabled = it
            binding.calculateDirverButton.isEnabled = it
        }

        mapActionVM.navInfor.observe(this) { info ->

            val speed = info.speed.coerceAtLeast(0.0)

            if (speed >= 1000) {
                binding.speed.text = "${speed / 1000.0}m/h"
            } else {
                binding.speed.text = "${speed}m/h"
            }



            val currentDistance = info.distane
            val currentDistanceString = if (currentDistance >= 1000) {
                "已导航${currentDistance / 1000.0}KM"
            } else {
                "已导航${currentDistance}M"
            }

            val totalDistance = info.totalDis
            val totalDistanceString = if (totalDistance >= 1000) {
                "预计行程${currentDistance / 1000.0}KM"
            } else {
                "预计行程${currentDistance}M"
            }

            val restTripDistance = totalDistance - currentDistance
            val restTripDistanceString = if (restTripDistance>=1000){
                    "余下行程${restTripDistance / 1000.0}KM"
            }else if(restTripDistance>=0){
                "余下行程${restTripDistance}M"
            }else if (restTripDistance<=-1000){
                "超过行程${restTripDistance/(-1000)}KM"
            }else{
                "超过行程${restTripDistance.absoluteValue}M"
            }

            binding.distance.text = "${totalDistanceString}\n${currentDistanceString}\n$restTripDistanceString"

            var time = info.time
            val hours = time / 3600
            val minutes = (time % 3600) / 60
            val seconds = time % 60
            val timeString = if (hours>1){
                "已导航${String.format("%d:%02d:%02d", hours, minutes, seconds)}/s"
            }else if (time<60){
                "已导航${time}/s"
            }else{
                "已导航${String.format("%02d:%02d", minutes, seconds)}/s"
            }

            binding.time.text = timeString
        }

        mapActionVM.tagPOI.observe(this) { data ->

            if (data.poiID != null) {

                binding.showTag.visibility = View.VISIBLE
                binding.title.text = "${data.name}"
                binding.tagMessage.text = "${data.address}\n${data.disdanceMessage} "

                mapActionVM.navPlaneStatus.value = true

                binding.calculateRiderButton.setOnClickListener {
                    mapActionVM.navReadyStatus.value = false
                    iMapFragment.navRiderLocation(data.latLng)
                }
                binding.calculateWalkButton.setOnClickListener {
                    mapActionVM.navReadyStatus.value = false
                    iMapFragment.navWalkLocation(data.latLng)
                }
                binding.calculateDirverButton.setOnClickListener {
                    mapActionVM.navReadyStatus.value = false
                    iMapFragment.navDiverLocation(data.latLng)
                }


//                binding.navButton.setOnClickListener {
////                    val routeSearch = RouteSearch(this)
////                    routeSearch.setRouteSearchListener(object : RouteSearch.OnRouteSearchListener {
////
////                        override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
////
////                        }
////
////                        override fun onDriveRouteSearched(p0: DriveRouteResult?, p1: Int) {
////
////                        }
////
////                        override fun onWalkRouteSearched(result: WalkRouteResult, errorCode: Int) {
////                            if (errorCode == 1000 && result != null && result.paths != null) {
////                                if (result.paths.size > 0) {
////                                    // 获取第一条步行路径
////                                    val walkPath = result.paths[0]
////                                    // 绘制路径到地图
//////                                    drawWalkRoute(walkPath)
////                                }
////                            } else {
////                                Toast.makeText(
////                                    this@MapsActivity,
////                                    "路径规划失败：$errorCode",
////                                    Toast.LENGTH_SHORT
////                                ).show()
////                            }
////                        }
////
////                        override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
//////                            TODO("Not yet implemented")
////                        }
////                    })
////
////// 设置起终点
////
////                    val orginPoint = iMapFragment.getPresionLocation()
////
////// 设置起终点
////                    val fromAndTo = RouteSearch.FromAndTo(
////                        LatLonPoint(orginPoint.getLat(), orginPoint.getLng()),  // 起点经纬度
////                        LatLonPoint(data.latLng.getLat(), data.latLng.getLng()) // 终点经纬度
////                    )
////// 构建查询对象
////// 构建查询对象
////                    val query = RouteSearch.WalkRouteQuery(fromAndTo)
////                    routeSearch.calculateWalkRouteAsyn(query)
//
//
//
//                    mapActionVM.locationLatLng.value = iMapFragment.getPresionLocation()
////                    Log.i("NavFragment","${mapActionVM.locationLatLng.value}")
////                    mapActionVM.state.value = InstanceMapActionVM.UIState.NAV()
//
//                    startActivity(Intent(this,NavSimpleActivity::class.java))
//
//                }

            } else {

                binding.showTag.visibility = View.GONE

            }


        }

        mapActionVM.state.observe(this){
            when(it){
               is InstanceMapActionVM.UIState.NAV->{
                   binding.navDetailView.visibility = View.VISIBLE
                   binding.showTag.visibility = View.GONE
               }
                else->{
                    binding.navDetailView.visibility = View.GONE
                    binding.showTag.visibility = View.GONE
                }
            }
        }

        binding.navButton.setOnClickListener {
            iMapFragment.startSelectMapNavLocation()
        }

        binding.navEmualButton.setOnClickListener {
            iMapFragment.startSelectMapNavLocationFormEmulator()
        }


        val density = this.resources.displayMetrics.density
        iMapFragment.routerPlaneBottomPadding = (240 * density).roundToInt()


    }


}