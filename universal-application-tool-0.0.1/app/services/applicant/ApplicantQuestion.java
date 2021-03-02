package services.applicant;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import services.question.QuestionDefinition;
import services.question.QuestionType;
import services.question.TranslationNotFoundException;

/**
 * Represents a question in the context of a specific applicant. Contains non-static inner classes
 * that represent the question as a specific question type (e.g. {@link NameQuestion}). These inner
 * classes provide access to the applicant's answer for the question. They can also implement
 * server-side validation logic.
 */
public class ApplicantQuestion {

  private final QuestionDefinition questionDefinition;
  private final ApplicantData applicantData;

  public ApplicantQuestion(QuestionDefinition definition) {
    this(definition, new ApplicantData());
  }

  ApplicantQuestion(QuestionDefinition questionDefinition, ApplicantData applicantData) {
    this.questionDefinition = checkNotNull(questionDefinition);
    this.applicantData = checkNotNull(applicantData);
  }

  public QuestionType getType() {
    return questionDefinition.getQuestionType();
  }

  public String getQuestionText() {
    try {
      return questionDefinition.getQuestionText(applicantData.preferredLocale());
    } catch (TranslationNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public TextQuestion getTextQuestion() {
    if (!getType().equals(QuestionType.TEXT)) {
      throw new RuntimeException(
          "Question is not a TEXT question: " + questionDefinition.getPath());
    }

    return new TextQuestion();
  }

  public NameQuestion getNameQuestion() {
    if (!getType().equals(QuestionType.NAME)) {
      throw new RuntimeException(
          "Question is not a NAME question: " + questionDefinition.getPath());
    }

    return new NameQuestion();
  }

  public class TextQuestion {
    private Optional<String> textValue;

    public boolean hasValue() {
      return getTextValue().isPresent();
    }

    public Optional<String> getTextValue() {
      if (textValue != null) {
        return textValue;
      }

      textValue = applicantData.readString(questionDefinition.getPath());

      return textValue;
    }

    public String getTextPath() {
      return questionDefinition.getPath();
    }
  }

  public class NameQuestion {
    private Optional<String> firstNameValue;
    private Optional<String> middleNameValue;
    private Optional<String> lastNameValue;

    public boolean hasFirstNameValue() {
      return getFirstNameValue().isPresent();
    }

    public boolean hasMiddleNameValue() {
      return getMiddleNameValue().isPresent();
    }

    public boolean hasLastNameValue() {
      return getLastNameValue().isPresent();
    }

    public Optional<String> getFirstNameValue() {
      if (firstNameValue != null) {
        return firstNameValue;
      }

      firstNameValue = applicantData.readString(getFirstNamePath());

      return firstNameValue;
    }

    public Optional<String> getMiddleNameValue() {
      if (middleNameValue != null) {
        return middleNameValue;
      }

      middleNameValue = applicantData.readString(getMiddleNamePath());

      return middleNameValue;
    }

    public Optional<String> getLastNameValue() {
      if (lastNameValue != null) {
        return lastNameValue;
      }

      lastNameValue = applicantData.readString(getLastNamePath());

      return lastNameValue;
    }

    // TODO: implement named methods for retrieving scalar paths instead of concatenating strings.
    public String getMiddleNamePath() {
      return questionDefinition.getPath() + ".middle";
    }

    // TODO: implement named methods for retrieving scalar paths instead of concatenating strings.
    public String getFirstNamePath() {
      return questionDefinition.getPath() + ".first";
    }

    // TODO: implement named methods for retrieving scalar paths instead of concatenating strings.
    public String getLastNamePath() {
      return questionDefinition.getPath() + ".last";
    }
  }
}
