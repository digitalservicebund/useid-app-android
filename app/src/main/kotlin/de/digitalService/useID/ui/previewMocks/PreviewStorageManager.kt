package de.digitalService.useID.ui.previewMocks

import de.digitalService.useID.StorageManagerType

class PreviewStorageManager : StorageManagerType {
    override fun getIsFirstTimeUser(): Boolean = false
    override fun setIsNotFirstTimeUser() {}
}
