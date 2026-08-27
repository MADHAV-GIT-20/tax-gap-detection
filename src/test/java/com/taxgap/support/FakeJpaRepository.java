package com.taxgap.support;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Test-only base that provides "not implemented" versions of every JpaRepository
 * method, so a fake repository in a test only has to override the few methods it
 * actually uses. This lets the tests run on plain JUnit without Mockito.
 */
public abstract class FakeJpaRepository<T, ID> implements JpaRepository<T, ID> {

    private UnsupportedOperationException nope() {
        return new UnsupportedOperationException("not implemented in this fake");
    }

    @Override public <S extends T> S save(S entity) { throw nope(); }
    @Override public <S extends T> List<S> saveAll(Iterable<S> entities) { throw nope(); }
    @Override public Optional<T> findById(ID id) { throw nope(); }
    @Override public boolean existsById(ID id) { throw nope(); }
    @Override public List<T> findAll() { throw nope(); }
    @Override public List<T> findAllById(Iterable<ID> ids) { throw nope(); }
    @Override public long count() { throw nope(); }
    @Override public void deleteById(ID id) { throw nope(); }
    @Override public void delete(T entity) { throw nope(); }
    @Override public void deleteAllById(Iterable<? extends ID> ids) { throw nope(); }
    @Override public void deleteAll(Iterable<? extends T> entities) { throw nope(); }
    @Override public void deleteAll() { throw nope(); }
    @Override public List<T> findAll(Sort sort) { throw nope(); }
    @Override public Page<T> findAll(Pageable pageable) { throw nope(); }
    @Override public void flush() { throw nope(); }
    @Override public <S extends T> S saveAndFlush(S entity) { throw nope(); }
    @Override public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) { throw nope(); }
    @Override public void deleteAllInBatch(Iterable<T> entities) { throw nope(); }
    @Override public void deleteAllByIdInBatch(Iterable<ID> ids) { throw nope(); }
    @Override public void deleteAllInBatch() { throw nope(); }
    @Override public T getOne(ID id) { throw nope(); }
    @Override public T getById(ID id) { throw nope(); }
    @Override public T getReferenceById(ID id) { throw nope(); }
    @Override public <S extends T> Optional<S> findOne(Example<S> example) { throw nope(); }
    @Override public <S extends T> List<S> findAll(Example<S> example) { throw nope(); }
    @Override public <S extends T> List<S> findAll(Example<S> example, Sort sort) { throw nope(); }
    @Override public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) { throw nope(); }
    @Override public <S extends T> long count(Example<S> example) { throw nope(); }
    @Override public <S extends T> boolean exists(Example<S> example) { throw nope(); }
    @Override public <S extends T, R> R findBy(Example<S> example,
            Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw nope(); }
}
