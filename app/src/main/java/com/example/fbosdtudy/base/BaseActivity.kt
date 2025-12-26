package com.example.fbosdtudy.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * @author Y³
 * @since 2025/9/2
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected abstract val bindingInflater: (LayoutInflater) -> VB
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
        initView()
        initViewModel()
    }

    abstract fun initView()

    open fun initViewModel() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}