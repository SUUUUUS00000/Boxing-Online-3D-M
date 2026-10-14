package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.viewmodel.GameViewModel

@Composable
fun ChatScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val messages by viewModel.chatMessages.collectAsState()
  var inputText by remember { mutableStateOf("") }

  val quickPills = listOf(
    "Good fight!",
    "Who wants to spar?",
    "Nice combo!",
    "Ready for rematch?",
    "OB3D physics rules!"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF090D16))
      .padding(16.dp)
  ) {

    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text("GLOBAL ARENA CHAT", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("Fighter Lounge", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("2,480 Online", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Messages Feed
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      reverseLayout = true,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages) { msg ->
        ChatMessageCard(msg)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Quick Taunt / Message Pills
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(quickPills) { pill ->
        SuggestionChip(
          onClick = { viewModel.sendChatMessage(pill) },
          label = { Text(pill, fontSize = 11.sp, color = Color(0xFFE2E8F0)) },
          colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = Color(0xFF1E293B)
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Message Input Field
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = { Text("Type message to boxers...", fontSize = 13.sp) },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedContainerColor = Color(0xFF131B2E),
          unfocusedContainerColor = Color(0xFF131B2E)
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            viewModel.sendChatMessage(inputText)
            inputText = ""
          }
        },
        modifier = Modifier
          .size(50.dp)
          .background(Color(0xFF2563EB), RoundedCornerShape(14.dp))
          .testTag("send_chat_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
      }
    }
  }
}

@Composable
fun ChatMessageCard(msg: ChatMessage) {
  val isPlayer = msg.isPlayer
  val tierColor = Color(msg.senderRank.colorHex)

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
  ) {
    Card(
      shape = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = if (isPlayer) 14.dp else 2.dp,
        bottomEnd = if (isPlayer) 2.dp else 14.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isPlayer) Color(0xFF1D4ED8) else Color(0xFF131B2E)
      ),
      modifier = Modifier.widthIn(max = 280.dp)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = msg.senderRank.getIcon(),
            contentDescription = null,
            tint = if (isPlayer) Color.White else tierColor,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = msg.senderName,
            color = if (isPlayer) Color(0xFF93C5FD) else tierColor,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp
          )
          Spacer(modifier = Modifier.weight(1f))
          Text(
            text = msg.timeAgo,
            color = Color(0xFF94A3B8),
            fontSize = 9.sp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = msg.message,
          color = Color.White,
          fontSize = 13.sp
        )
      }
    }
  }
}
