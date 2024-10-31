
package com.cs407.square_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
import com.cs407.square_up.R

class AddNewGroupMember : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_group_member, container, false)

        return view
    }
}
