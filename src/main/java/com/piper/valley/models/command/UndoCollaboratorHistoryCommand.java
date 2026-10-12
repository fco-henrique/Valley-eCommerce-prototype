package com.piper.valley.models.command;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.AddStoreCollaboratorForm;
import com.piper.valley.models.domain.StoreCollabHistory;
import com.piper.valley.models.enums.StoreHistoryType;
import com.piper.valley.models.service.AuthService;
import com.piper.valley.models.service.StoreService;

/** One request instance; the receiver keeps ownership of writes and new history entries. */
public class UndoCollaboratorHistoryCommand implements Command<Boolean> {
    private final StoreCollabHistory history;
    private final CurrentUser currentUser;
    private final AuthService authService;
    private final StoreService storeService;

    public UndoCollaboratorHistoryCommand(StoreCollabHistory history, CurrentUser currentUser,
            AuthService authService, StoreService storeService) {
        this.history = history;
        this.currentUser = currentUser;
        this.authService = authService;
        this.storeService = storeService;
    }

    @Override
    public Boolean execute() {
        if (!authService.canAccessStore(history.getStore(), currentUser)) {
            return false;
        }

        if (history.getType() == StoreHistoryType.ADD) {
            storeService.removeCollaboratorToStore(
                    new AddStoreCollaboratorForm(history.getStore().getId(),
                            history.getCollab().getUsername()), currentUser.getId());
        } else if (history.getType() == StoreHistoryType.DELETE) {
            storeService.addCollaboratorToStore(
                    new AddStoreCollaboratorForm(history.getStore().getId(),
                            history.getCollab().getUsername()), currentUser.getId());
        }

        // Preserve the legacy successful no-op for other history types (including EDIT).
        return true;
    }
}
