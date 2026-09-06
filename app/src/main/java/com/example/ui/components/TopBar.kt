package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GolapiTopBar(
    siteName: String,
    tagline: String,
    isAdminMode: Boolean,
    onToggleAdmin: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchVisible: Boolean,
    onToggleSearch: () -> Unit,
    onOpenWebPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureWhite)
    ) {
        // Upper utility bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅 রবিবার, ০৬ সেপ্টেম্বর ২০২৬  |  ঢাকা",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Netlify preview button
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RoseDeep,
                    modifier = Modifier.clickable { onOpenWebPreview() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Netlify Web View",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "লাইভ ওয়েব",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Admin button
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isAdminMode) RosePrimary else Slate800,
                    modifier = Modifier.clickable { onToggleAdmin() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isAdminMode) "অ্যাডমিন চালু" else "অ্যাডমিন প্যানেল",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Main Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RoseContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "গোলাপি",
                            color = RosePrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নিউজ",
                        color = Slate900,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                Text(
                    text = tagline,
                    color = Slate600,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate100, RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "অনুসন্ধান",
                    tint = Slate800,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Search Input Expandable
        if (isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("সংবাদ শিরোনাম বা বিষয় অনুসন্ধান করুন...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosePrimary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = Slate50,
                    unfocusedContainerColor = Slate50
                )
            )
        }

        Divider(color = RosePrimary, thickness = 2.dp)
    }
}
