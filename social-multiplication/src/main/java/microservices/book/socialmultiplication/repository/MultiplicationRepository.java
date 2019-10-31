package microservices.book.socialmultiplication.repository;

import microservices.book.socialmultiplication.domain.Multiplication;
import org.springframework.data.repository.CrudRepository;

public interface MultiplicationRepository extends CrudRepository<Multiplication, Long> {
}
