/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.app.features.form

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.request.RequestOptions
import im.vector.app.R
import im.vector.app.core.epoxy.ClickListener
import im.vector.app.core.epoxy.VectorEpoxyHolder
import im.vector.app.core.epoxy.onClick
import im.vector.app.core.glide.GlideApp
import im.vector.app.features.home.AvatarRenderer

@EpoxyModelClass(layout = R.layout.item_editable_avatar)
abstract class FormEditableAvatarItem : EpoxyModelWithHolder<FormEditableAvatarItem.Holder>() {

    @EpoxyAttribute
    lateinit var avatarRenderer: AvatarRenderer

    @EpoxyAttribute
    var enabled: Boolean = true

    @EpoxyAttribute
    var imageUri: Uri? = null

    @EpoxyAttribute
    var clickListener: ClickListener? = null

    @EpoxyAttribute
    var deleteListener: ClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.imageContainer.onClick(clickListener?.takeIf { enabled })
        GlideApp.with(holder.image)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.image)
        holder.delete.isVisible = imageUri != null
        holder.delete.onClick(deleteListener?.takeIf { enabled })
    }

    class Holder : VectorEpoxyHolder() {
        val imageContainer by bind<View>(R.id.itemEditableAvatarImageContainer)
        val image by bind<ImageView>(R.id.itemEditableAvatarImage)
        val delete by bind<View>(R.id.itemEditableAvatarDelete)
    }
}
