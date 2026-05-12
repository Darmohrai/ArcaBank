package com.arcabank.core_finance.service.grpc;

import com.arcabank.core_finance.dto.AccountDto;
import com.arcabank.core_finance.dto.AccountOnlyRequest;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.notificator.engine.Notificator;
import com.arcabank.core_finance.repository.AccountRepository;
import com.arcabank.core_finance.service.AccountService;
import com.arcabank.grpc.AccountProvisioningServiceGrpc;
import com.arcabank.grpc.CreateAccountRequest;
import com.arcabank.grpc.CreateAccountResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AccountProvisioningGrpcServer extends AccountProvisioningServiceGrpc.AccountProvisioningServiceImplBase {

    private final AccountService accountService;

    @Override
    @Transactional
    public void createInitialAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
            log.info("gRPC: Creating an initial account (WITHOUT CARD) for User ID: {}", request.getUserId());

            UUID userId = UUID.fromString(request.getUserId());

            String currencyCode = request.getCurrency().name().replace("CURRENCY_", "");
            if (currencyCode.equals("UNSPECIFIED")) {
                currencyCode = "UAH";
            }

            AccountOnlyRequest creationRequest = new AccountOnlyRequest(
                currencyCode,
                AccountType.CHECKING
            );

            AccountDto newAccount = accountService.openNewAccount(userId, creationRequest);

            CreateAccountResponse response = CreateAccountResponse.newBuilder()
                .setAccountId(newAccount.getId().toString())
                .setSuccess(true)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: The account has been successfully generated and saved!");

        } catch (Exception e) {
            log.error("gRPC: Account generation error: ", e);
            responseObserver.onNext(CreateAccountResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
        }
    }
}
