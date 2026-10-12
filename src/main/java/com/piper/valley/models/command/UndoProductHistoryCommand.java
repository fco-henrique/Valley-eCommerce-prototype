package com.piper.valley.models.command;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.AddStoreProductForm;
import com.piper.valley.models.domain.StoreProduct;
import com.piper.valley.models.domain.StoreProductHistory;
import com.piper.valley.models.enums.StoreHistoryType;
import com.piper.valley.models.service.AuthService;
import com.piper.valley.models.service.StoreProductService;
import com.piper.valley.models.service.StoreService;

import java.util.Optional;

/** Reverses a product history entry through the existing store services. */
public class UndoProductHistoryCommand implements Command<Boolean> {
    private final StoreProductHistory history;
    private final CurrentUser currentUser;
    private final AuthService authService;
    private final StoreService storeService;
    private final StoreProductService storeProductService;

    public UndoProductHistoryCommand(StoreProductHistory history, CurrentUser currentUser,
            AuthService authService, StoreService storeService,
            StoreProductService storeProductService) {
        this.history = history;
        this.currentUser = currentUser;
        this.authService = authService;
        this.storeService = storeService;
        this.storeProductService = storeProductService;
    }

    @Override
    public Boolean execute() {
        if (!authService.canAccessStore(history.getStore(), currentUser)) {
            return false;
        }

        if (history.getType() == StoreHistoryType.DELETE) {
            // As before, restoration creates a new store product rather than restoring its old ID.
            storeService.addProductToStore(new AddStoreProductForm(
                    history.getStoreId(), history.getProductId(), history.getOldProductName(),
                    history.getOldProductDescription(), history.getOldProductPrice()),
                    currentUser.getUser());
            return true;
        }

        Optional<StoreProduct> storeProduct = storeProductService.getProductById(history.getStoreProductID());
        if (!storeProduct.isPresent()) {
            return false;
        }

        if (history.getType() == StoreHistoryType.ADD) {
            // Preserve legacy behavior: the receiver's Boolean result is not used.
            storeService.removeProductFromStore(storeProduct.get().getId(), currentUser.getUser());
        }

        return true;
    }
}
