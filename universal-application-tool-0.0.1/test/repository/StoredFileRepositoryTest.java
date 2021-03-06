package repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import models.StoredFile;
import org.junit.Test;
import play.db.ebean.EbeanConfig;

public class StoredFileRepositoryTest extends WithPostgresContainer {

  @Test
  public void createFile() {
    AmazonS3Client s3ClientMock = mock(AmazonS3Client.class);
    EbeanConfig ebeanConfig = instanceOf(EbeanConfig.class);
    DatabaseExecutionContext dbec = instanceOf(DatabaseExecutionContext.class);
    StoredFileRepository repo = new StoredFileRepository(ebeanConfig, s3ClientMock, dbec);
    StoredFile file = new StoredFile();
    file.setName("file name");
    byte[] content = new String("file content").getBytes(StandardCharsets.UTF_8);
    file.setContent(content);
    when(s3ClientMock.getObject(any(String.class))).thenReturn(content);

    repo.insert(file).toCompletableFuture().join();

    long id = file.id;
    StoredFile f = repo.lookupFile(id).toCompletableFuture().join().get();
    assertThat(f.id).isEqualTo(id);
    assertThat(f.getName()).isEqualTo("file name");
    assertThat(f.getContent()).isEqualTo(content);
  }
}
