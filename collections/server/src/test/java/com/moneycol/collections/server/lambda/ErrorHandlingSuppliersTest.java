package com.moneycol.collections.server.lambda;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.infrastructure.repository.FirebaseCollectionRepository;
import com.moneycol.collections.server.infrastructure.repository.FirestoreProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

public class ErrorHandlingSuppliersTest {

    @ParameterizedTest
    @ValueSource(classes = {ExecutionException.class, InterruptedException.class})
    public void testWrappedCheckedExceptionsAreHandled(Class<? extends Throwable> checkedExceptionToThrow) {

        // Given: an operation in firestore that throws a checked exception
        Firestore f = Mockito.mock(Firestore.class);
        FirebaseCollectionRepository firebaseCollectionRepo = mockFirebaseRepo(f);
        DocumentReference dr = mockCollectionAndDocument(f);
        mockApiFutureThrowingException(dr, checkedExceptionToThrow);

        // When: calling the operation
        // Then: the exception is wrapped as RuntimeException
        // And: is with the correct cause
        // And: is handled separately
        Collection c = prepareDummyCollection();
        assertThatThrownBy(() -> firebaseCollectionRepo.createWithSupplier(c))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(checkedExceptionToThrow);
    }

    private Collection prepareDummyCollection() {
        Collector collector = Collector.of(CollectorId.of(CollectorId.randomId()));
        return Collection.withNameAndDescription(
                CollectionId.of(CollectionId.randomId()),
                "name", "desc", collector);
    }

    private FirebaseCollectionRepository mockFirebaseRepo(Firestore f) {
        FirestoreProvider firestoreProvider = Mockito.mock(FirestoreProvider.class);
        Mockito.when(firestoreProvider.getFirestoreInstance()).thenReturn(f);
        return new FirebaseCollectionRepository(firestoreProvider);
    }

    private DocumentReference mockCollectionAndDocument(Firestore f) {
        CollectionReference cr = Mockito.mock(CollectionReference.class);
        DocumentReference dr = Mockito.mock(DocumentReference.class);
        Mockito.when(cr.document(anyString())).thenReturn(dr);
        Mockito.when(f.collection(anyString())).thenReturn(cr);
        return dr;
    }

    @SneakyThrows
    private void mockApiFutureThrowingException(DocumentReference dr, Class<? extends Throwable> exceptionToThrow) {
        ApiFuture<WriteResult> writeResultApiFuture = (ApiFuture<WriteResult>) Mockito.mock(ApiFuture.class);
        Mockito.when(dr.set(anyMap())).thenReturn(writeResultApiFuture);

        // Throw with specific error
        Mockito.when(writeResultApiFuture.get()).thenThrow(exceptionToThrow);
    }
}
