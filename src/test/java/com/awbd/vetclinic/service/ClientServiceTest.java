package com.awbd.vetclinic.service;

import com.awbd.vetclinic.model.Client;
import com.awbd.vetclinic.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private final Long clientId = 1L;
    @Mock
    private ClientRepository clientRepository;
    private ClientService clientService;
    private Client client;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository);
        client = new Client();
        client.setId(clientId);
        client.setName("Alice Smith");
        client.setEmail("alice@example.com");
        client.setPhone("0712345678");
    }

    @Test
    @DisplayName("Should create a client")
    void create_ShouldSaveClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        Client saved = clientService.create(client);
        assertNotNull(saved);
        assertEquals("Alice Smith", saved.getName());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Should update client details")
    void update_ShouldUpdateClient() {
        Client details = new Client();
        details.setName("Alice Updated");
        details.setPhone("0799999999");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client updated = clientService.update(clientId, details);

        assertEquals("Alice Updated", updated.getName());
        assertEquals("0799999999", updated.getPhone());
        verify(clientRepository).save(any(Client.class));
    }
}
