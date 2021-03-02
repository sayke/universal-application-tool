package controllers.applicant;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import play.i18n.Messages;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.applicant.ApplicantService;
import services.applicant.Block;
import services.program.ProgramDefinition;
import services.program.ProgramService;
import views.applicant.ProgramIndexView;

/** Controller for handling methods for an applicant applying to programs. */
public class ApplicantProgramsController extends Controller {

  private final HttpExecutionContext httpContext;
  private final ApplicantService applicantService;
  private final ApplicantMessages applicantMessages;
  private final ProgramService programService;
  private final ProgramIndexView programIndexView;

  @Inject
  public ApplicantProgramsController(
      HttpExecutionContext httpContext,
      ApplicantService applicantService,
      ApplicantMessages applicantMessages,
      ProgramService programService,
      ProgramIndexView programIndexView) {
    this.httpContext = httpContext;
    this.applicantService = applicantService;
    this.applicantMessages = checkNotNull(applicantMessages);
    this.programService = checkNotNull(programService);
    this.programIndexView = checkNotNull(programIndexView);
  }

  public CompletionStage<Result> index(Request request, long applicantId) {
    CompletableFuture<ImmutableList<ProgramDefinition>> programsFuture =
        programService.listProgramDefinitionsAsync().toCompletableFuture();
    CompletableFuture<Messages> messagesFuture =
        applicantMessages.getMessagesForCurrentApplicant(request).toCompletableFuture();
    return CompletableFuture.allOf(programsFuture, messagesFuture)
        .thenApplyAsync(
            (v) -> {
              ImmutableList<ProgramDefinition> programs = programsFuture.join();
              Messages messages = messagesFuture.join();
              return ok(programIndexView.render(messages, applicantId, programs));
            },
            httpContext.current());
  }

  // TODO(https://github.com/seattle-uat/universal-application-tool/issues/224): Get next incomplete
  // block instead of just first block.
  public CompletionStage<Result> edit(long applicantId, long programId) {
    // Determine first incomplete block, then redirect to other edit.
    return applicantService
        .getReadOnlyApplicantProgramService(applicantId, programId)
        .thenApplyAsync(
            roApplicantService -> {
              Optional<Block> block = roApplicantService.getFirstIncompleteBlock();
              if (block.isPresent()) {
                return found(
                    routes.ApplicantProgramBlocksController.edit(
                        applicantId, programId, block.get().getId()));
              } else {
                // TODO(https://github.com/seattle-uat/universal-application-tool/issues/256): All
                // blocks are filled in, so redirect to end of program submission.
                return found(routes.ApplicantProgramsController.index(applicantId));
              }
            },
            httpContext.current());
  }
}
