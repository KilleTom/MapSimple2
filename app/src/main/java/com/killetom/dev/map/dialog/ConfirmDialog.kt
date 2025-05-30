package com.killetom.dev.map.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.killetom.dev.map.databinding.DialogConfirmBinding


class ConfirmDialog(
    private  val title:String,
    private  val message:String,
    private val confirmAction:((ConfirmDialog)->Unit),
    private val cancelAction:((ConfirmDialog)->Unit)) :DialogFragment(){



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = DialogConfirmBinding.inflate(inflater,container,false)

        binding.title.text = title
        binding.message.text = message

        binding.confirmButton.setOnClickListener {
            confirmAction.invoke(this)
        }

        binding.cancelButton.setOnClickListener {
            cancelAction.invoke(this)
        }

        return binding.root
    }

//    override fun onStart() {
//        super.onStart()
//        val dialog = dialog ?: return
//        val window = dialog.window ?: return
//        //设置背景色透明
//        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        val params = window.attributes
//        params.width =
//        //设置屏幕透明度 0.0f~1.0f(完全透明~完全不透明)
//        params.dimAmount = 0.5f
//        window.attributes = params
//    }

}