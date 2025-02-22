package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4

data class DropdownOption(
    val text: String,
    val onSelect: () -> Unit
)

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier, options: List<DropdownOption>, default: DropdownOption) {
    val isExpanded = remember { mutableStateOf(false) }
    val selected = remember { mutableStateOf(default) }
    Box(modifier.width(108.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .width(108.dp)
                    .clip(RoundedCornerShape(45.dp))
                    .clickable { isExpanded.value = !isExpanded.value }
                    .background(Color.White)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selected.value.text, style = h4.copy(colorResource(R.color.dark_green)))
                Image(
                    painterResource(
                        if (isExpanded.value)
                            R.drawable.dropdown_collapse
                        else R.drawable.dropdown_expand),
                    if (isExpanded.value) "Collapse dropdown" else "Expand dropdown"
                )
            }

            AnimatedVisibility(
                visible = isExpanded.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column (modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                ) {
                    for (option in options) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selected.value = option
                                    option.onSelect()
                                    isExpanded.value = false
                                }
                                .padding(10.dp)
                        ) {
                            Text(
                                option.text,
                                style = h4.copy(colorResource(R.color.dark_green))
                            )
                        }
                        if (option != options.last()) {
                            Spacer(
                                modifier = Modifier
                                    .height(0.5.dp).fillMaxWidth().padding(horizontal = 10.dp).background(
                                        colorResource(R.color.spacer_grey)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}