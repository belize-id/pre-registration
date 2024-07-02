/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorCodes;
import io.mosip.preregistration.application.errorcodes.TransliterationErrorMessage;
import io.mosip.preregistration.application.exception.MandatoryFieldRequiredException;
import io.mosip.preregistration.application.exception.UnSupportedLanguageException;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

import java.util.List;

/**
 * This class provides the service implementation for Transliteration
 * application.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Service
public class TransliterationService {

	/**
	 * Autowired reference
	 */
	@Autowired
	private Transliteration<String> translitrator;

	/**
	 * Autowired reference for {@link #serviceUtil}
	 */
	@Autowired
	private TransliterationServiceUtil serviceUtil;

	@Autowired
	private Environment environment;

	private static final String RESIDENT_TRANSLITERATION_WORKAROUND_PROPERTY = "resident-transliteration-workaround-for-%s-%s";
	private static final int LANGUAGE_LIST_SIZE = 2;
	public static final String HYPHEN = "-";
	public static final String COMMA = ",";
	/**
	 * 
	 * This method is used to transliterate the given data.
	 * 
	 * @param requestDTO
	 * @return responseDto with transliterated value
	 */
	public MainResponseDTO<TransliterationResponseDTO> translitratorService(
			MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		MainResponseDTO<TransliterationResponseDTO> responseDTO = new MainResponseDTO<>();
		responseDTO.setId(requestDTO.getId());
		responseDTO.setVersion(requestDTO.getVersion());
		try {
			TransliterationRequestDTO transliterationRequestDTO = requestDTO.getRequest();
			if (serviceUtil.isEntryFieldsNull(transliterationRequestDTO)) {
				String toFieldValue = translitrator.transliterate(transliterationRequestDTO.getFromFieldLang(),
						transliterationRequestDTO.getToFieldLang(), transliterationRequestDTO.getFromFieldValue());
				responseDTO.setResponse(serviceUtil.responseSetter(toFieldValue, transliterationRequestDTO));
				responseDTO.setResponsetime(serviceUtil.getCurrentResponseTime());
			} else {
				throw new MandatoryFieldRequiredException(TransliterationErrorCodes.PRG_TRL_APP_002.getCode(),
						TransliterationErrorMessage.INCORRECT_MANDATORY_FIELDS.getMessage(), responseDTO);
			}
		} catch (Exception e) {
			throw new UnSupportedLanguageException(TransliterationErrorCodes.PRG_TRL_APP_002.getCode(),
					TransliterationErrorMessage.UNSUPPORTED_LANGUAGE.getMessage(), responseDTO);
		}
		return responseDTO;
	}
	public MainResponseDTO<TransliterationResponseDTO> getMainResponseDTOResponseEntity(MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		String propertyValue = environment.getProperty(String.format(RESIDENT_TRANSLITERATION_WORKAROUND_PROPERTY,
				requestDTO.getRequest().getFromFieldLang(), requestDTO.getRequest().getToFieldLang()));
		if (propertyValue != null) {
			List<String> propertyValueList = List.of(propertyValue.split(COMMA));
			MainResponseDTO<TransliterationResponseDTO> responseDTO = null;
			for(String languagePair:propertyValueList){
				MainRequestDTO<TransliterationRequestDTO> transliterationRequestDTOMainRequestDTO = new MainRequestDTO<>();
				TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
				List<String> languageList = List.of(languagePair.split(HYPHEN));
				if(languageList.size() == LANGUAGE_LIST_SIZE){
					transliterationRequestDTO.setFromFieldLang(languageList.get(0));
					transliterationRequestDTO.setToFieldLang(languageList.get(1));
					if(responseDTO!=null){
						transliterationRequestDTO.setFromFieldValue(responseDTO.getResponse().getToFieldValue());
					} else {
						transliterationRequestDTO.setFromFieldValue(requestDTO.getRequest().getFromFieldValue());
					}
					transliterationRequestDTOMainRequestDTO.setRequest(transliterationRequestDTO);
					transliterationRequestDTOMainRequestDTO.setId(requestDTO.getId());
					transliterationRequestDTOMainRequestDTO.setVersion(requestDTO.getVersion());
					transliterationRequestDTOMainRequestDTO.setRequesttime(requestDTO.getRequesttime());
					responseDTO = translitratorService(transliterationRequestDTOMainRequestDTO);
				}
			}
			if(responseDTO!=null && responseDTO.getResponse()!=null){
				TransliterationResponseDTO transliterationResponseDTO = responseDTO.getResponse();
				transliterationResponseDTO.setToFieldLang(requestDTO.getRequest().getToFieldLang());
				transliterationResponseDTO.setFromFieldValue(requestDTO.getRequest().getFromFieldValue());
				transliterationResponseDTO.setFromFieldLang(requestDTO.getRequest().getFromFieldLang());
				responseDTO.setResponse(transliterationResponseDTO);
			}
			return responseDTO;

		} else {
			return translitratorService(requestDTO);
		}
	}

}
