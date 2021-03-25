package com.vnptit.videocallsample.module

import com.vnptit.videocallsample.home.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * Created by Ông Hoàng Nhật Phương on 2/18/2021.
 * VNPT COM
 * phuonghn@vnpt.vn
 */

val viewModule : Module = module {
   // single { androidContext() }
    viewModel{
        MainViewModel(get())
    }
}