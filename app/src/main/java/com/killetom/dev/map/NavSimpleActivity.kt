package com.killetom.dev.map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.killetom.dev.map.databinding.ActivityMapsBinding
import com.killetom.dev.map.databinding.ActivityNavSimpleBinding
import com.killetom.dev.map.vm.InstanceMapActionVM

class NavSimpleActivity : AppCompatActivity() {

    private lateinit var binding :ActivityNavSimpleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityNavSimpleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val application :MapSimpleApp = this.application as  MapSimpleApp
        val mapActionVM: InstanceMapActionVM =
            ViewModelProvider(application.getGlobalViewModelStore(),
                ViewModelProvider.NewInstanceFactory()
            )[InstanceMapActionVM::class.java]

        val navFragment :NavFragment = NavFragment(mapActionVM)

        supportFragmentManager.beginTransaction().add(binding.navContent.id, navFragment).commitNow()

    }
}