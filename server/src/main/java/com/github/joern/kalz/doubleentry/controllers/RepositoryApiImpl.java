package com.github.joern.kalz.doubleentry.controllers;

import com.github.joern.kalz.doubleentry.generated.api.RepositoryApi;
import com.github.joern.kalz.doubleentry.generated.model.ApiRepository;
import com.github.joern.kalz.doubleentry.services.repository.GetRepositoryResponse;
import com.github.joern.kalz.doubleentry.services.repository.ImportRepositoryRequest;
import com.github.joern.kalz.doubleentry.services.repository.RepositoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RepositoryApiImpl implements RepositoryApi {

    private final RepositoryService repositoryService;
    private final ResponseFactory responseFactory;
    private final RequestFactory requestFactory;

    public RepositoryApiImpl(RepositoryService repositoryService, ResponseFactory responseFactory,
                             RequestFactory requestFactory) {
        this.repositoryService = repositoryService;
        this.responseFactory = responseFactory;
        this.requestFactory = requestFactory;
    }

    @Override
    public ResponseEntity<ApiRepository> exportRepository() {
        GetRepositoryResponse getRepositoryResponse = repositoryService.getRepository();

        ApiRepository responseBody = new ApiRepository();
        responseBody.setAccounts(getRepositoryResponse.getAccounts().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList()));
        responseBody.setTransactions(getRepositoryResponse.getTransactions().stream()
                .map(responseFactory::convertToResponse)
                .collect(Collectors.toList()));

        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> importRepository(@Valid ApiRepository repository) {
        ImportRepositoryRequest request = requestFactory.convertToRequest(repository);
        repositoryService.importRepository(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
