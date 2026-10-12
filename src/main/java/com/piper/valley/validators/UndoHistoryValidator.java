package com.piper.valley.validators;

import com.piper.valley.forms.UndoHistoryForm;
import com.piper.valley.models.domain.Store;
import com.piper.valley.models.domain.StoreHistory;
import com.piper.valley.models.enums.StoreHistoryStatus;
import com.piper.valley.models.service.StoreHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UndoHistoryValidator extends AbstractExistingStoreResourceValidator<UndoHistoryForm, StoreHistory> {
    @Autowired
    private StoreHistoryService storeHistoryService;

    @Override
    protected Class<UndoHistoryForm> formType() {
        return UndoHistoryForm.class;
    }

    @Override
    protected Optional<StoreHistory> findResource(UndoHistoryForm form) {
        return storeHistoryService.getById(form.getId());
    }

    @Override
    protected String fieldName() {
        return "Id";
    }

    @Override
    protected String missingMessage() {
        return "History doesn't exist!";
    }

    @Override
    protected boolean isValidState(StoreHistory history) {
        return history.getStatus() == StoreHistoryStatus.UNDOABLE;
    }

    @Override
    protected String invalidStateMessage() {
        return null;
    }

    @Override
    protected Store storeOf(StoreHistory history) {
        return history.getStore();
    }
}
