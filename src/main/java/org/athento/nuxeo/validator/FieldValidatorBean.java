/**
 * 
 */
package org.athento.nuxeo.validator;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.xml.bind.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Name;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

/**
 * @author athento
 *
 */
@Name("bFieldValidator")
public class FieldValidatorBean implements Serializable {

	public static final String NIFletters = "TRWAGMYFPDXBNJZSQVHLCKET";

	public boolean accept() {
		if (_log.isDebugEnabled()) {
			_log.debug("Accepting form");
		}
		return true;
	}

	public void validateBankAccountNumber_byParts(FacesContext context, UIComponent component,
			Object value) {
		String bankCode = retrieveInputComponentValue_asString(component,
				"bankCodeInputId");
		String branchCode = retrieveInputComponentValue_asString(component,
				"branchCodeInputId");
		String controlDigit = retrieveInputComponentValue_asString(component,
				"dcInputId");
		String accountNumber = retrieveInputComponentValue_asString(component,
				"accountNumberInputId");
		if (isValidBankAccountNumber(bankCode, branchCode, controlDigit, accountNumber)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context,
							"label.error.validator.bank.account.number"), null);
			throw new ValidatorException(message);
		}
	}

	public void validateBankAccountNumber_allInOne(FacesContext context,
			UIComponent component, Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating bank account: " + value);
		}
		String ban = (String) value;
		String bankCode = ban.substring(0, 4);
		String branchCode = ban.substring(4, 8);
		String controlDigit = ban.substring(8, 10);
		String accountNumber = ban.substring(10, 20);

		if (isValidBankAccountNumber(bankCode, branchCode, controlDigit, accountNumber)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context,
							"label.error.validator.bank.account.number"), null);
			throw new ValidatorException(message);
		}
	}

	public void validateEmailAddress(FacesContext context,
			UIComponent component, Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating emailAddress: " + value);
		}
		String emailAddress = (String) value;
		String expression = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
		if (isValid(emailAddress, expression)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context, "label.error.validator.email"), null);
			throw new ValidatorException(message);
		}
	}

	public void validateHomePhoneNumber(FacesContext context,
			UIComponent component, Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating homePhoneNumber: " + value);
		}
		String phoneNumber = (String) value;
		String expression = "^[89]\\d{8}$";
		if (isValid(phoneNumber, expression)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context, "label.error.validator.homePhoneNumber"),
					null);
			throw new ValidatorException(message);
		}
	}

	public void validateMobilePhoneNumber(FacesContext context,
			UIComponent component, Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating mobilePhoneNumber: " + value);
		}
		String phoneNumber = (String) value;
		String expression = "^[67]\\d{8}$";
		if (isValid(phoneNumber, expression)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR,
					ComponentUtils.translate(context,
							"label.error.validator.mobilePhoneNumber"), null);
			throw new ValidatorException(message);
		}
	}

	public void validateNIF(FacesContext context, UIComponent component,
			Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating nif: " + value);
		}
		String nif = (String) value;

		if (nif.toUpperCase().startsWith("X")
				|| nif.toUpperCase().startsWith("Y")
				|| nif.toUpperCase().startsWith("Z")) {
			nif = nif.substring(1);
			if (_log.isDebugEnabled()) {
				_log.debug("It's a NIE. Removing first char: " + nif);
			}
		}
		try {
			if (isValidNIF(nif)) {
				Long number = Long
						.parseLong(nif.substring(0, nif.length() - 1));
				String letter = nif.substring(nif.length() - 1, nif.length());
				int pos = (int) (number % 23);
				String matchingLetter = FieldValidatorBean.NIFletters
						.substring(pos, pos + 1);
				if (!(letter.toUpperCase().equals(matchingLetter.toUpperCase()))) {
					throw new ValidationException(ComponentUtils.translate(
							context, "label.error.validator.nifLetter"));
				}
			} else {
				throw new ValidationException(ComponentUtils.translate(context,
						"label.error.validator.nifFormat"));
			}
		} catch (ValidationException e) {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
			throw new ValidatorException(message);
		}
		return; // all validations are ok
	}

	public void validateNIFLazy(FacesContext context, UIComponent component,
			Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating nif lazy: " + value);
		}
		String nif = (String) value;
		if (isValidNIF(nif)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context, "label.error.validator.homePhoneNumber"),
					null);
			throw new ValidatorException(message);
		}
	}

	public void validateText(FacesContext context, UIComponent component,
			Object value) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating text: " + value);
		}
		String theValue = (String) value;
		String expression = "^[a-zA-Z0-9]{0,30}$";
		if (isValid(theValue, expression)) {
			// do nothing as the given string is well-formed
			return;
		} else {
			// display an error in the input form
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_ERROR, ComponentUtils.translate(
							context, "label.error.validator.text"), null);
			throw new ValidatorException(message);
		}
	}

	private String getControlDigit(String bankCode, String branchCode,
			String accountNumber) {
		String rsum1;
		String rsum2;
		rsum1 = mod11("00" + bankCode + branchCode);
		rsum2 = mod11(accountNumber);
		return rsum1 + rsum2;
	}

	private boolean isValid(String value, String regexp) {
		if (_log.isDebugEnabled()) {
			_log.debug("Validating [" + value + "] against regexp [" + regexp
					+ "]");
		}
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			if (_log.isDebugEnabled()) {
				_log.debug("Match successful for value: " + value);
			}
			return true;
		} else {
			if (_log.isDebugEnabled()) {
				_log.debug("Value do not match regular expression: " + regexp);
			}
			return false;
		}
	}

	private boolean isValidNIF(String nif) {
		if (_log.isDebugEnabled()) {
			_log.debug("is a valid NIF?: " + nif);
		}
		String expression = "(\\d{8})([A-Za-z]{1})";
		if (isValid(nif, expression)) {
			if (_log.isDebugEnabled()) {
				_log.debug("it is :)");
			}
			return true;
		} else {
			if (_log.isDebugEnabled()) {
				_log.debug("it's not :(");
			}
			return false;
		}
	}

	private String mod11(String str) {
		int[] digits = new int[] { 1, 2, 4, 8, 5, 10, 9, 7, 3, 6 };
		int sum = 0;
		for (int i = 0; i != 10; i++) {
			sum += (Integer.parseInt(str.substring(i, i + 1)) * digits[i]);
		}
		sum = 11 - sum % 11;
		if (sum == 11)
			sum = 0;
		if (sum == 10)
			sum = 1;
		return String.valueOf(sum);
	}

	private Object retrieveInputComponentValue(UIComponent anchor,
			String componentId) {
		Map attributes = anchor.getAttributes();
		String inputId = (String) attributes.get(componentId);
		UIInput component = (UIInput) anchor.findComponent(inputId);
		return component.getLocalValue();
	}

	private String retrieveInputComponentValue_asString(UIComponent anchor,
			String componentId) {
		Object o = retrieveInputComponentValue(anchor, componentId);
		return (o!=null?o.toString():null);
	}

	private boolean isValidBankAccountNumber(
		String bankCode, String branchCode, String controlDigit, String accountNumber) {
		String cd = getControlDigit(bankCode, branchCode, accountNumber);
		return controlDigit.equals(cd);
	}

	private static Log _log = LogFactory.getLog(FieldValidatorBean.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
