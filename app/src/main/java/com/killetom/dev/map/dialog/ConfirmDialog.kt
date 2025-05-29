package com.killetom.dev.map.dialog

import androidx.fragment.app.DialogFragment

class ConfirmDialog(
    val title:String,
    val message:String,
    val confirmAction:(()->Unit),
    val cancelAction:(()->Unit)?=null) :DialogFragment(){



}