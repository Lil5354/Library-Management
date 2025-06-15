package com.uef.library.repository;

import com.uef.library.model.MarqueeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarqueeMessageRepository extends JpaRepository<MarqueeMessage, Long> {

}