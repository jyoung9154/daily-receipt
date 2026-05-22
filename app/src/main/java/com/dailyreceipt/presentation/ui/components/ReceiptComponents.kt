package com.dailyreceipt.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyreceipt.presentation.theme.CreamBackground
import com.dailyreceipt.presentation.theme.DottedBorder

@Composable
fun ReceiptCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = DottedBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(CreamBackground)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                color = DottedBorder,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun DottedDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(vertical = 8.dp)
    ) {
        drawLine(
            color = DottedBorder,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    }
}

@Composable
fun ReceiptSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = DottedBorder,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
        DottedDivider()
    }
}
