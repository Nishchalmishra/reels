package arivihan.technologies.doubtbuzzter2.compose.component.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage

@Composable
fun ProfileHeaderComponent(
    username: String?,
    isSubscribed: Boolean,
    isNeet: Boolean,
    logoUrl: String?,
    tags: List<String>,
    onHeaderClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    if (isSubscribed) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onHeaderClick() }
        ) {
            val greeting = if (isNeet) {
                if (username.isNullOrEmpty()) "Hello, Dr. Beta" else "Hello, Dr. $username"
            } else {
                if (username.isNullOrEmpty()) "Hello, Beta" else "Hello, $username"
            }
            
            Text(
                text = greeting,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            if (!isNeet && username.isNullOrEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit Name",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEditNameClick() },
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    url = logoUrl,
                    modifier = Modifier.size(40.dp)
                )
            } else if (isNeet) {
                Icon(
                    painter = painterResource(id = R.drawable.neet_icon),
                    contentDescription = "NEET",
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onHeaderClick() }
        ) {
            tags.forEach { tag ->
                ProfileTag(text = tag)
            }
        }
    }
}

@Composable
fun ProfileTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F0F0))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}
