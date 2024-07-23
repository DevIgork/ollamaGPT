import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ollamachat.databinding.ItemContainerRecevidedMessageBinding
import com.example.ollamachat.databinding.ItemContainerSentMessageBinding
import com.example.ollamachat.model.ChatMessage

class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemContainerSentMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemContainerRecevidedMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SentMessageViewHolder) {
            holder.setData(chatMessages[position])
        } else if (holder is ReceivedMessageViewHolder) {
            holder.setData(chatMessages[position])
        }
    }

    class SentMessageViewHolder(
        private val binding: ItemContainerSentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
        }
    }

    class ReceivedMessageViewHolder(
        private val binding: ItemContainerRecevidedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
        }
    }
}
