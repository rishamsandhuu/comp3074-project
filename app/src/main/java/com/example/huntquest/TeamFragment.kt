@file:Suppress("DEPRECATION")

package com.example.huntquest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible

class TeamFragment : Fragment() {

    private lateinit var about: View
    private lateinit var btnInfo: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_team, container, false)
        about = v.findViewById(R.id.aboutOverlay)
        btnInfo = v.findViewById(R.id.btnInfo)

        btnInfo.setOnClickListener { toggleAbout() }

        // If the overlay is visible, back button hides it instead of leaving the screen
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (about.isVisible) {
                hideAbout()
            } else {
                // Let Activity handle normal back (go Home)
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }

        return v
    }

    private fun toggleAbout() {
        if (about.isVisible) hideAbout() else showAbout()
    }

    private fun showAbout() {
        about.visibility = View.VISIBLE
        fade(about, inAnim = true)
    }

    private fun hideAbout() {
        fade(about, inAnim = false) {
            about.visibility = View.GONE
        }
    }

    private fun fade(view: View, inAnim: Boolean, end: (() -> Unit)? = null) {
        val anim = if (inAnim) AlphaAnimation(0f, 1f) else AlphaAnimation(1f, 0f)
        anim.duration = 180
        anim.fillAfter = true
        anim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation) { end?.invoke() }
        })
        view.startAnimation(anim)
    }
}
