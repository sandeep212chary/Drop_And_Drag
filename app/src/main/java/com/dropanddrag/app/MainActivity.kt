package com.dropanddrag.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.dropanddrag.app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), View.OnTouchListener {

    private var imagesEncodedList: ArrayList<Uri>? = arrayListOf()
    private lateinit var binding: ActivityMainBinding
    private var lastPoint = Point()
    private var relativeLayout: RelativeLayout? = null
    private var lastOutOfTop = false
    private var lastOutOfLeft = false
    private var lastOutOfRight = false
    private var lastOutOfBottom = false
    private val permissionsRequired = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        relativeLayout = binding.layout
        imagesEncodedList!!.clear()

        if (!permissionIfNeeded()) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, permissionsRequired, 241
                )
            }

        }

        binding.addBtn.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent)
        }


    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data!!.clipData != null) {
                    val count: Int = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri: Uri = data.clipData!!.getItemAt(i).uri
                        imagesEncodedList!!.add(imageUri)
                        updateUI(imageUri)
                    }
                } else {
                    val imagePath: Uri? = data.data!!
                    if (imagePath != null) {
                        imagesEncodedList!!.add(imagePath)
                        updateUI(imagePath)
                    }
                }

            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
            }

        }


    private fun updateUI(uri: Uri) {
        val imageView = ImageView(this@MainActivity)
        imageView.setImageURI(uri)
        addView(imageView)
        imageView.setOnTouchListener(this)
    }

    private fun addView(imageView: ImageView) {
        val params = RelativeLayout.LayoutParams(400, 400)

        // setting the margin in linearlayout
        params.setMargins(0, 10, 0, 10)
        imageView.layoutParams = params

        // adding the image in layout
        binding.layout.addView(imageView)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {

        val point = Point(event.rawX.toInt(), event.rawY.toInt())
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN ->
                lastPoint = point
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_POINTER_UP -> {}
            MotionEvent.ACTION_MOVE -> {
                val offset = Point(point.x - lastPoint.x, point.y - lastPoint.y)
                val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
                layoutParams.leftMargin += offset.x
                layoutParams.topMargin += offset.y
                layoutParams.rightMargin =
                    relativeLayout?.measuredWidth!! - layoutParams.leftMargin + view.measuredWidth
                layoutParams.bottomMargin =
                    relativeLayout?.measuredHeight!! - layoutParams.topMargin + view.measuredHeight
                view.layoutParams = layoutParams
                lastPoint = point
            }
        }

        val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
        val outOfTop = layoutParams.topMargin < 0
        val outOfLeft = layoutParams.leftMargin < 0
        val outOfBottom: Boolean =
            layoutParams.topMargin + view.measuredHeight > relativeLayout!!.measuredHeight
        val outOfRight: Boolean =
            layoutParams.leftMargin + view.measuredWidth > relativeLayout!!.measuredWidth
        lastOutOfTop = outOfTop
        lastOutOfLeft = outOfLeft
        lastOutOfBottom = outOfBottom
        lastOutOfRight = outOfRight
        return true
    }

    private fun permissionIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
                return true
            }
        }
        return false
    }
}