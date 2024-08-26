import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ollamachat.R
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
            binding.textMessage.setTextColor(binding.root.context.getColor(R.color.black))

            binding.textMessage.setOnClickListener {
                showPopupMenu(it, chatMessage.message)
            }
        }

        private fun showPopupMenu(view: View, message: String) {
            val popupMenu = PopupMenu(view.context, view)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_copy -> {
                        copyTextToClipboard(view.context, message)
                        true
                    }
                    R.id.menu_play_audio -> {
                        playAudio(view.context)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun copyTextToClipboard(context: Context?, message: String) {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        private fun playAudio(context: Context?) {
            Toast.makeText(context, "Work in progress.", Toast.LENGTH_SHORT).show()
        }
    }

    class ReceivedMessageViewHolder(
        private val binding: ItemContainerRecevidedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textMessage.setTextColor(binding.root.context.getColor(R.color.black))

            binding.textMessage.setOnClickListener {
                showPopupMenu(it, chatMessage.message)
            }

        }

        private fun showPopupMenu(view: View, message: String) {
            val popupMenu = PopupMenu(view.context, view)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_copy -> {
                        copyTextToClipboard(view.context, message)
                        true
                    }
                    R.id.menu_play_audio -> {
                        playAudio(view.context)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun copyTextToClipboard(context: Context?, message: String) {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        private fun playAudio(context: Context?) {
            Toast.makeText(context, "Work in progress.", Toast.LENGTH_SHORT).show()
        }
    }
}
