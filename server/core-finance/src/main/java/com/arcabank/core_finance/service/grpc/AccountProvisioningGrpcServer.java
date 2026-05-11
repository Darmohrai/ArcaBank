package com.arcabank.core_finance.service.grpc;

import com.arcabank.core_finance.dto.AccountCreationRequest;
import com.arcabank.core_finance.dto.AccountResponse;
import com.arcabank.core_finance.model.util.AccountType;
import com.arcabank.core_finance.service.AccountService;
import com.arcabank.grpc.AccountProvisioningServiceGrpc;
import com.arcabank.grpc.CreateAccountRequest;
import com.arcabank.grpc.CreateAccountResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class AccountProvisioningGrpcServer extends AccountProvisioningServiceGrpc.AccountProvisioningServiceImplBase {

    private final AccountService accountService;

    @Override
    public void createInitialAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
            log.info("gRPC: Creating an initial account for User ID: {}", request.getUserId());

            UUID userId = UUID.fromString(request.getUserId());

            String currencyCode = request.getCurrency().name().replace("CURRENCY_", "");
            if (currencyCode.equals("UNSPECIFIED")) {
                currencyCode = "UAH";
            }

            AccountCreationRequest creationRequest = new AccountCreationRequest(currencyCode, AccountType.CHECKING);

            AccountResponse newAccount = accountService.createAccountWithCard(
                userId,
                creationRequest,
                request.getFirstName(),
                request.getLastName());

            CreateAccountResponse response = CreateAccountResponse.newBuilder()
                .setAccountId(newAccount.accountId().toString())
                .setSuccess(true)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("gRPC: The account has been successfully generated and saved!");

        } catch (Exception e) {
            log.error("gRPC: Invoice generation error: ", e);
            responseObserver.onNext(CreateAccountResponse.newBuilder().setSuccess(false).build());
            responseObserver.onCompleted();
        }
    }
}
