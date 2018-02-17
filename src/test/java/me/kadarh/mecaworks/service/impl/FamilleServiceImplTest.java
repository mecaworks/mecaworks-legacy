package me.kadarh.mecaworks.service.impl;

import me.kadarh.mecaworks.domain.others.Famille;
import me.kadarh.mecaworks.repo.others.FamilleRepo;
import me.kadarh.mecaworks.service.FamilleService;
import me.kadarh.mecaworks.service.exceptions.OperationFailedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class FamilleServiceImplTest {

	@Mock
	private FamilleRepo familleRepo;

	private FamilleService familleService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		familleService = new FamilleServiceImpl(familleRepo);
	}

	@Test
	public void add() {
		//given
		Famille famille = new Famille();
		famille.setNom("FamilleEE");

		//when
		when(familleRepo.save(famille)).thenReturn(famille);
		Famille famille1 = familleService.add(famille);

		//then
		verify(familleRepo, times(1)).save(any(Famille.class));
		assertEquals(famille.getNom(), famille1.getNom());
	}

	@Test
	public void update() {
		//given
		Famille famille = new Famille();
		famille.setId(77L);
		famille.setNom("F1");

		//when
		when(familleRepo.save(famille)).thenReturn(famille);
		famille.setNom("F2");
		when(familleRepo.findById(77L)).thenReturn(Optional.of(famille));


		//then
		assertEquals(famille.getId(), familleService.update(famille).getId());
		assertEquals(famille.getNom(), familleService.update(famille).getNom());
		verify(familleRepo, times(2)).save(any(Famille.class));
		verify(familleRepo, times(2)).findById(anyLong());
	}

	@Test(expected = OperationFailedException.class)
	public void updateError() {
		//given
		Famille famille = new Famille();
		famille.setId(77L);
		famille.setNom("Famille1");
		familleRepo.save(famille);

		//when
		famille.setNom("Famille2");
		when(familleRepo.findById(88L)).thenReturn(Optional.of(famille));
		when(familleRepo.save(famille)).thenReturn(famille);

		//then
		assertEquals(famille.getId(), familleService.update(famille).getId());
		verify(familleRepo, times(2)).save(any(Famille.class));
		verify(familleRepo, times(1)).findById(anyLong());
	}

	@Test
	public void familleList() {

	}

}