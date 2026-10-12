package com.piper.valley.validators;

import com.piper.valley.forms.AddBrandForm;
import com.piper.valley.forms.AddCompanyForm;
import com.piper.valley.forms.AddStoreCollaboratorForm;
import com.piper.valley.forms.DemoteAdminForm;
import com.piper.valley.forms.FinishOrderForm;
import com.piper.valley.forms.PromoteAdminForm;
import com.piper.valley.forms.UndoHistoryForm;
import com.piper.valley.models.domain.Brand;
import com.piper.valley.models.domain.Company;
import com.piper.valley.models.domain.Order;
import com.piper.valley.models.repository.BrandRepository;
import com.piper.valley.models.repository.CompanyRepository;
import com.piper.valley.models.service.OrderService;
import com.piper.valley.models.service.StoreHistoryService;
import com.piper.valley.models.service.UserService;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TemplateMethodValidatorTest {
    @Test
    public void existingErrorsSkipResourceLookup() {
        FinishOrderValidator validator = new FinishOrderValidator();
        OrderService service = mock(OrderService.class);
        ReflectionTestUtils.setField(validator, "orderService", service);
        FinishOrderForm form = new FinishOrderForm();
        form.setOrderId(1L);
        BeanPropertyBindingResult errors = errors(form);
        errors.rejectValue("orderId", "NotValid");

        validator.validate(form, errors);

        verifyZeroInteractions(service);
        assertTrue(validator.supports(FinishOrderForm.class));
        assertFalse(validator.supports(UndoHistoryForm.class));
    }

    @Test
    public void resourceTemplatesRejectMissingAndInvalidResources() {
        FinishOrderValidator finish = new FinishOrderValidator();
        OrderService orderService = mock(OrderService.class);
        ReflectionTestUtils.setField(finish, "orderService", orderService);
        FinishOrderForm finishForm = new FinishOrderForm();
        finishForm.setOrderId(1L);
        when(orderService.getOrderById(1L)).thenReturn(Optional.<Order>empty());
        BeanPropertyBindingResult missingOrder = errors(finishForm);
        finish.validate(finishForm, missingOrder);
        assertEquals("NotValid", missingOrder.getFieldError("orderId").getCode());

        when(orderService.getOrderById(1L)).thenReturn(Optional.of(new Order()));
        BeanPropertyBindingResult invalidOrder = errors(finishForm);
        finish.validate(finishForm, invalidOrder);
        assertEquals("Un-valid Order", invalidOrder.getFieldError("orderId").getDefaultMessage());

        UndoHistoryValidator undo = new UndoHistoryValidator();
        StoreHistoryService historyService = mock(StoreHistoryService.class);
        ReflectionTestUtils.setField(undo, "storeHistoryService", historyService);
        UndoHistoryForm undoForm = new UndoHistoryForm();
        undoForm.setId(2L);
        when(historyService.getById(2L)).thenReturn(Optional.empty());
        BeanPropertyBindingResult missingHistory = errors(undoForm);
        undo.validate(undoForm, missingHistory);
        assertEquals("History doesn't exist!", missingHistory.getFieldError("Id").getDefaultMessage());
    }

    @Test
    public void collaboratorAndAdminTemplatesKeepTheirOwnErrorCodes() {
        UserService userService = mock(UserService.class);
        when(userService.getUserByUsername("someone")).thenReturn(Optional.empty());

        AddStoreCollaboratorForm collaboratorForm = new AddStoreCollaboratorForm(1L, "someone");
        AddCollaboratorFormValidator add = new AddCollaboratorFormValidator();
        RemoveCollaboratorFormValidator remove = new RemoveCollaboratorFormValidator();
        ReflectionTestUtils.setField(add, "userService", userService);
        ReflectionTestUtils.setField(remove, "userService", userService);
        BeanPropertyBindingResult addErrors = errors(collaboratorForm);
        BeanPropertyBindingResult removeErrors = errors(collaboratorForm);
        add.validate(collaboratorForm, addErrors);
        remove.validate(collaboratorForm, removeErrors);
        assertEquals("msg.UsernameNotFound", addErrors.getFieldError("username").getCode());
        assertEquals("msg.NotValide", removeErrors.getFieldError("username").getCode());

        PromoteAdminForm promoteForm = new PromoteAdminForm();
        promoteForm.setUsername("someone");
        DemoteAdminForm demoteForm = new DemoteAdminForm();
        demoteForm.setUsername("someone");
        PromoteAdminFormValidator promote = new PromoteAdminFormValidator();
        DemoteAdminFormValidator demote = new DemoteAdminFormValidator();
        ReflectionTestUtils.setField(promote, "userService", userService);
        ReflectionTestUtils.setField(demote, "userService", userService);
        BeanPropertyBindingResult promoteErrors = errors(promoteForm);
        BeanPropertyBindingResult demoteErrors = errors(demoteForm);
        promote.validate(promoteForm, promoteErrors);
        demote.validate(demoteForm, demoteErrors);
        assertEquals("NotValid", promoteErrors.getFieldError("username").getCode());
        assertEquals("msg.UsernameNotFound", demoteErrors.getFieldError("username").getCode());
    }

    @Test
    public void uniqueNameTemplateUsesSpecificRepositoryAndMessage() {
        AddBrandFormValidator brandValidator = new AddBrandFormValidator();
        BrandRepository brandRepository = mock(BrandRepository.class);
        ReflectionTestUtils.setField(brandValidator, "brandRepository", brandRepository);
        AddBrandForm brandForm = new AddBrandForm();
        brandForm.setName("duplicate");
        when(brandRepository.findOneByName("duplicate")).thenReturn(Optional.of(new Brand()));
        BeanPropertyBindingResult brandErrors = errors(brandForm);
        brandValidator.validate(brandForm, brandErrors);
        assertEquals("msg.DuplicateBrandName", brandErrors.getFieldError("name").getCode());

        AddCompanyFormValidator companyValidator = new AddCompanyFormValidator();
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ReflectionTestUtils.setField(companyValidator, "companyRepository", companyRepository);
        AddCompanyForm companyForm = new AddCompanyForm();
        companyForm.setName("duplicate");
        when(companyRepository.findOneByName("duplicate")).thenReturn(Optional.of(new Company()));
        BeanPropertyBindingResult companyErrors = errors(companyForm);
        companyValidator.validate(companyForm, companyErrors);
        assertEquals("msg.DuplicateCompanyName", companyErrors.getFieldError("name").getCode());
    }

    private BeanPropertyBindingResult errors(Object form) {
        return new BeanPropertyBindingResult(form, "form");
    }
}
