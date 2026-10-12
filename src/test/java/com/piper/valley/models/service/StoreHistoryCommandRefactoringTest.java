package com.piper.valley.models.service;

import com.piper.valley.auth.CurrentUser;
import com.piper.valley.forms.AddStoreCollaboratorForm;
import com.piper.valley.forms.AddStoreProductForm;
import com.piper.valley.models.domain.*;
import com.piper.valley.models.enums.StoreHistoryStatus;
import com.piper.valley.models.enums.StoreHistoryType;
import com.piper.valley.models.repository.StoreHistoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/** Characterization tests run against the service before and after extraction. */
public class StoreHistoryCommandRefactoringTest {
    private StoreHistoryRepository histories;
    private AuthService auth;
    private StoreService stores;
    private StoreProductService products;
    private StoreHistoryServiceImpl service;
    private CurrentUser currentUser;
    private User actor;
    private Store store;

    @Before
    public void setUp() {
        histories = mock(StoreHistoryRepository.class);
        auth = mock(AuthService.class);
        stores = mock(StoreService.class);
        products = mock(StoreProductService.class);
        currentUser = mock(CurrentUser.class);
        actor = new User(7L);
        store = new PhysicalStore();
        store.setId(10L);
        when(currentUser.getId()).thenReturn(7L);
        when(currentUser.getUser()).thenReturn(actor);
        when(auth.canAccessStore(store, currentUser)).thenReturn(true);
        service = new StoreHistoryServiceImpl();
        ReflectionTestUtils.setField(service, "storeHistoryRepository", histories);
        ReflectionTestUtils.setField(service, "authService", auth);
        ReflectionTestUtils.setField(service, "storeService", stores);
        ReflectionTestUtils.setField(service, "storeProductService", products);
    }

    @Test
    public void missingHistoryReturnsFalseWithoutSideEffects() {
        assertFalse(service.undo(99L, currentUser));
        verify(histories).findOne(99L);
        verifyNoMoreInteractions(histories);
        verifyZeroInteractions(auth, stores, products);
    }

    @Test
    public void unknownHistorySubtypeReturnsFalseWithoutFinalizing() {
        StoreHistory history = prepare(new StoreHistory(), StoreHistoryType.ADD);
        assertFalse(service.undo(99L, currentUser));
        assertNotFinalized(history);
        verifyZeroInteractions(auth, stores, products);
    }

    @Test
    public void unauthorizedCollaboratorUndoDoesNotMutate() {
        StoreCollabHistory history = collaboratorHistory(StoreHistoryType.ADD);
        assertDenied(history);
    }

    @Test
    public void unauthorizedProductUndoDoesNotMutate() {
        StoreProductHistory history = productHistory(StoreHistoryType.DELETE);
        assertDenied(history);
    }

    @Test
    public void undoAddedCollaboratorRemovesItBeforeFinalizing() {
        StoreCollabHistory history = collaboratorHistory(StoreHistoryType.ADD);
        assertTrue(service.undo(99L, currentUser));
        ArgumentCaptor<AddStoreCollaboratorForm> form = ArgumentCaptor.forClass(AddStoreCollaboratorForm.class);
        InOrder order = inOrder(auth, stores, histories);
        order.verify(histories).findOne(99L);
        order.verify(auth).canAccessStore(store, currentUser);
        order.verify(stores).removeCollaboratorToStore(form.capture(), eq(7L));
        order.verify(histories).save(history);
        assertCollaboratorForm(form.getValue());
        assertEquals(StoreHistoryStatus.FINAL, history.getStatus());
        verifyNoMoreInteractions(histories, stores, auth);
    }

    @Test
    public void undoDeletedCollaboratorAddsItBeforeFinalizing() {
        StoreCollabHistory history = collaboratorHistory(StoreHistoryType.DELETE);
        assertTrue(service.undo(99L, currentUser));
        ArgumentCaptor<AddStoreCollaboratorForm> form = ArgumentCaptor.forClass(AddStoreCollaboratorForm.class);
        InOrder order = inOrder(stores, histories);
        order.verify(stores).addCollaboratorToStore(form.capture(), eq(7L));
        order.verify(histories).save(history);
        assertCollaboratorForm(form.getValue());
        assertEquals(StoreHistoryStatus.FINAL, history.getStatus());
        verifyNoMoreInteractions(stores);
    }

    @Test
    public void collaboratorEditRetainsLegacySuccessfulNoOp() {
        StoreCollabHistory history = collaboratorHistory(StoreHistoryType.EDIT);
        assertTrue(service.undo(99L, currentUser));
        verify(histories).save(history);
        verifyZeroInteractions(stores, products);
    }

    @Test
    public void undoDeletedProductRestoresSnapshotBeforeFinalizing() {
        StoreProductHistory history = productHistory(StoreHistoryType.DELETE);
        assertTrue(service.undo(99L, currentUser));
        ArgumentCaptor<AddStoreProductForm> form = ArgumentCaptor.forClass(AddStoreProductForm.class);
        InOrder order = inOrder(auth, stores, histories);
        order.verify(histories).findOne(99L);
        order.verify(auth).canAccessStore(store, currentUser);
        order.verify(stores).addProductToStore(form.capture(), same(actor));
        order.verify(histories).save(history);
        assertEquals(Long.valueOf(10L), form.getValue().getStoreId());
        assertEquals(Long.valueOf(20L), form.getValue().getProductId());
        assertEquals("Old name", form.getValue().getName());
        assertEquals("Old description", form.getValue().getDescription());
        assertEquals(12.5f, form.getValue().getPrice(), 0f);
        assertEquals(StoreHistoryStatus.FINAL, history.getStatus());
        verifyZeroInteractions(products);
        verifyNoMoreInteractions(stores, histories, auth);
    }

    @Test
    public void undoAddedProductRemovesLookupResultBeforeFinalizing() {
        StoreProductHistory history = productHistory(StoreHistoryType.ADD);
        existingProduct();
        when(stores.removeProductFromStore(31L, actor)).thenReturn(true);
        assertTrue(service.undo(99L, currentUser));
        InOrder order = inOrder(auth, products, stores, histories);
        order.verify(histories).findOne(99L);
        order.verify(auth).canAccessStore(store, currentUser);
        order.verify(products).getProductById(30L);
        order.verify(stores).removeProductFromStore(31L, actor);
        order.verify(histories).save(history);
        assertEquals(StoreHistoryStatus.FINAL, history.getStatus());
        verifyNoMoreInteractions(auth, products, stores, histories);
    }

    @Test
    public void missingAddedProductReturnsFalseWithoutFinalizing() {
        StoreProductHistory history = productHistory(StoreHistoryType.ADD);
        when(products.getProductById(30L)).thenReturn(Optional.<StoreProduct>empty());
        assertFalse(service.undo(99L, currentUser));
        assertNotFinalized(history);
        verifyZeroInteractions(stores);
    }

    @Test
    public void removalFalseRetainsLegacySuccessAndFinalization() {
        StoreProductHistory history = productHistory(StoreHistoryType.ADD);
        existingProduct();
        when(stores.removeProductFromStore(31L, actor)).thenReturn(false);
        assertTrue(service.undo(99L, currentUser));
        verify(histories).save(history);
    }

    @Test
    public void productEditRetainsLegacyLookupAndSuccessfulNoOp() {
        StoreProductHistory history = productHistory(StoreHistoryType.EDIT);
        existingProduct();
        assertTrue(service.undo(99L, currentUser));
        verify(products).getProductById(30L);
        verify(histories).save(history);
        verifyZeroInteractions(stores);
    }

    @Test
    public void missingEditedProductReturnsFalse() {
        StoreProductHistory history = productHistory(StoreHistoryType.EDIT);
        when(products.getProductById(30L)).thenReturn(Optional.<StoreProduct>empty());
        assertFalse(service.undo(99L, currentUser));
        assertNotFinalized(history);
        verifyZeroInteractions(stores);
    }

    @Test
    public void receiverExceptionPropagatesWithoutFinalizing() {
        StoreProductHistory history = productHistory(StoreHistoryType.DELETE);
        RuntimeException failure = new RuntimeException("save failed");
        when(stores.addProductToStore(any(AddStoreProductForm.class), same(actor))).thenThrow(failure);
        try {
            service.undo(99L, currentUser);
            fail("Expected receiver exception");
        } catch (RuntimeException actual) {
            assertSame(failure, actual);
            assertNotFinalized(history);
        }
    }

    @Test
    public void directUndoMethodsDoNotFinalizeHistory() {
        StoreCollabHistory collab = collaboratorHistory(StoreHistoryType.ADD);
        StoreProductHistory product = productHistory(StoreHistoryType.DELETE);
        assertTrue(service.undoCollab(collab, currentUser));
        assertTrue(service.undoProduct(product, currentUser));
        verifyZeroInteractions(histories);
        assertEquals(StoreHistoryStatus.UNDOABLE, collab.getStatus());
        assertEquals(StoreHistoryStatus.UNDOABLE, product.getStatus());
    }

    private void assertDenied(StoreHistory history) {
        when(auth.canAccessStore(store, currentUser)).thenReturn(false);
        assertFalse(service.undo(99L, currentUser));
        verify(auth).canAccessStore(store, currentUser);
        assertNotFinalized(history);
        verifyZeroInteractions(stores, products);
    }

    private void assertNotFinalized(StoreHistory history) {
        assertEquals(StoreHistoryStatus.UNDOABLE, history.getStatus());
        verify(histories, never()).save(any(StoreHistory.class));
    }

    private void assertCollaboratorForm(AddStoreCollaboratorForm form) {
        assertEquals(Long.valueOf(10L), form.getStoreId());
        assertEquals("collaborator", form.getUsername());
    }

    private void existingProduct() {
        StoreProduct product = new StoreProduct();
        product.setId(31L);
        when(products.getProductById(30L)).thenReturn(Optional.of(product));
    }

    private StoreCollabHistory collaboratorHistory(StoreHistoryType type) {
        StoreCollabHistory history = prepare(new StoreCollabHistory(), type);
        User collaborator = new User(8L);
        collaborator.setUsername("collaborator");
        history.setCollab(collaborator);
        return history;
    }

    private StoreProductHistory productHistory(StoreHistoryType type) {
        StoreProductHistory history = prepare(new StoreProductHistory(), type);
        history.setStoreId(10L);
        history.setProductId(20L);
        history.setStoreProductID(30L);
        history.setOldProductName("Old name");
        history.setOldProductDescription("Old description");
        history.setOldProductPrice(12.5f);
        return history;
    }

    private <T extends StoreHistory> T prepare(T history, StoreHistoryType type) {
        history.setId(99L);
        history.setStore(store);
        history.setType(type);
        history.setStatus(StoreHistoryStatus.UNDOABLE);
        when(histories.findOne(99L)).thenReturn(history);
        return history;
    }
}
