package com.killetom.dev.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMapException
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.navi.*
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.enums.TransportType
import com.amap.api.navi.model.*
import com.killetom.dev.map.databinding.FragmentNavBinding
import com.killetom.dev.map.tts.TTSManager
import com.killetom.dev.map.vm.InstanceMapActionVM


class NavFragment(private val mapActionVM: InstanceMapActionVM) :Fragment() , AMapNaviListener,
    AMapNaviViewListener {


    private val TAG = "NavFragment"

    private var binding: FragmentNavBinding? = null

    private var mAMapNavi :AMapNavi?=null

    private val tts = TTSManager()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.i(TAG, "onCreate")

            NaviSetting.updatePrivacyShow(context, true, true);
            NaviSetting.updatePrivacyAgree(context, true);


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.i(TAG, "onCreateView")
        val current = FragmentNavBinding.inflate(inflater, container, false)

        binding = current

        val navView = current.navView
        navView.onCreate(savedInstanceState)

        Log.i(TAG,"map was ${navView.map}")
        navView.setAMapNaviViewListener(this)

        navView.naviMode = AMapNaviView.NORTH_UP_MODE
        navView.setShowMode(3)
       val options =  navView.viewOptions.apply {
           isAutoDrawRoute = true
           isNaviArrowVisible = true
           isSettingMenuEnabled = true
           isTrafficLine = true
           isSensorEnable = true
       }
        navView.viewOptions = options

        // 初始化导航服务

        try {
            mAMapNavi = AMapNavi.getInstance(requireContext())

            mAMapNavi?.isNaviTravelView = true
            mAMapNavi?.addAMapNaviListener(this)
//            mAMapNavi?.addParallelRoadListener(this)
            mAMapNavi?.setUseInnerVoice(true, true)

            //设置模拟导航的行车速度
//            mAMapNavi?.setEmulatorNaviSpeed(75)

        } catch (e: AMapException) {
            e.printStackTrace()
        }

        // 初始化语音播报
        tts.init(requireContext())

//        current.start.isEnabled = false
//        current.start.setOnClickListener{
//            mAMapNavi?.startNavi(NaviType.GPS)
//        }


        return current.root
    }

    override fun onDestroyView() {

        binding?.navView?.onDestroy()
        binding = null
        tts.shutdown()
        mAMapNavi?.stopNavi()
        AMapNavi.destroy()


        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()

        binding?.navView?.onPause()
    }

    override fun onResume() {
        super.onResume()

        binding?.navView?.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding?.navView?.onSaveInstanceState(outState)
    }



    override fun onInitNaviFailure() {
       
    }

    override fun onInitNaviSuccess() {

        Log.i(TAG, "onInitNaviSuccess")
        // 设置步行导航类型
        mAMapNavi?.setTravelInfo(AMapTravelInfo(TransportType.Walk))

        val latLng = mapActionVM.locationLatLng.value?:return

        val tagPoint = mapActionVM.tagPOI.value?:return

        mAMapNavi?.calculateWalkRoute(
            NaviLatLng(latLng.getLat(),latLng.getLng()),
            NaviLatLng(tagPoint.latLng.getLat(),tagPoint.latLng.getLng())
        )



    }

    override fun onStartNavi(p0: Int) {
        val location = mapActionVM.locationLatLng.value?:return

        val latLng = LatLng(location.getLat(), location.getLng()) // 例如：北京天安门的位置

        Log.i(TAG, "move${latLng.toString()}")

        Log.i(TAG, "move${binding?.navView==null}")

        Log.i(TAG, "move${binding?.navView?.map==null}")

        binding?.navView?.map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onTrafficStatusUpdate() {
       
    }

    override fun onLocationChange(p0: AMapNaviLocation?) {
       
    }

    override fun onGetNavigationText(p0: Int, p1: String?) {
       
    }

    override fun onGetNavigationText(p0: String?) {
//        tts.speak(p0)
    }

    override fun onEndEmulatorNavi() {
       
    }

    override fun onArriveDestination() {
       
    }

    override fun onCalculateRouteFailure(p0: Int) {
       Toast.makeText(requireContext(),"生成路线出错code$p0",Toast.LENGTH_SHORT).show()
    }

    override fun onCalculateRouteFailure(p0: AMapCalcRouteResult?) {
       
    }

    override fun onReCalculateRouteForYaw() {
       
    }

    override fun onReCalculateRouteForTrafficJam() {
       
    }

    override fun onArrivedWayPoint(p0: Int) {
       
    }

    override fun onGpsOpenStatus(p0: Boolean) {
       
    }

    override fun onNaviInfoUpdate(p0: NaviInfo?) {
       
    }

    override fun updateCameraInfo(p0: Array<out AMapNaviCameraInfo>?) {
       
    }

    override fun updateIntervalCameraInfo(
        p0: AMapNaviCameraInfo?,
        p1: AMapNaviCameraInfo?,
        p2: Int
    ) {
       
    }

    override fun onServiceAreaUpdate(p0: Array<out AMapServiceAreaInfo>?) {
       
    }

    override fun showCross(p0: AMapNaviCross?) {
       
    }

    override fun hideCross() {
       
    }

    override fun showModeCross(p0: AMapModelCross?) {
       
    }

    override fun hideModeCross() {
       
    }

    override fun showLaneInfo(p0: Array<out AMapLaneInfo>?, p1: ByteArray?, p2: ByteArray?) {
       
    }

    override fun showLaneInfo(p0: AMapLaneInfo?) {
       
    }

    override fun hideLaneInfo() {
       
    }

    override fun onCalculateRouteSuccess(p0: IntArray?) {
//       binding?.start?.isEnabled = true
    }

    override fun onCalculateRouteSuccess(p0: AMapCalcRouteResult?) {
        mAMapNavi?.startNavi(NaviType.GPS)

    }

    override fun notifyParallelRoad(p0: Int) {
       
    }

    override fun OnUpdateTrafficFacility(p0: Array<out AMapNaviTrafficFacilityInfo>?) {
       
    }

    override fun OnUpdateTrafficFacility(p0: AMapNaviTrafficFacilityInfo?) {
       
    }

    override fun updateAimlessModeStatistics(p0: AimLessModeStat?) {
       
    }

    override fun updateAimlessModeCongestionInfo(p0: AimLessModeCongestionInfo?) {
       
    }

    override fun onPlayRing(p0: Int) {
       
    }

    override fun onNaviRouteNotify(p0: AMapNaviRouteNotifyData?) {
       
    }

    override fun onGpsSignalWeak(p0: Boolean) {
       
    }

    override fun onNaviSetting() {
       
    }

    override fun onNaviCancel() {
       
    }

    override fun onNaviBackClick(): Boolean {
       return true
    }

    override fun onNaviMapMode(p0: Int) {
       
    }

    override fun onNaviTurnClick() {
       
    }

    override fun onNextRoadClick() {
       
    }

    override fun onScanViewButtonClick() {
       
    }

    override fun onLockMap(p0: Boolean) {

    }

    override fun onNaviViewLoaded() {
       Log.i(TAG,"onNaviViewLoaded - map:${binding?.navView?.map}")
    }

    override fun onMapTypeChanged(p0: Int) {
       
    }

    override fun onNaviViewShowMode(p0: Int) {
       
    }
}