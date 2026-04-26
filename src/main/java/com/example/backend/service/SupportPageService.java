package com.example.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.SupportContact;
import com.example.backend.domain.entity.SupportIssueGuide;
import com.example.backend.domain.entity.SupportMaintenanceItem;
import com.example.backend.domain.entity.SupportResourceLink;
import com.example.backend.domain.request.support.ReqSupportContactDTO;
import com.example.backend.domain.request.support.ReqSupportIssueGuideDTO;
import com.example.backend.domain.request.support.ReqSupportMaintenanceItemDTO;
import com.example.backend.domain.request.support.ReqSupportResourceLinkDTO;
import com.example.backend.domain.response.support.ResSupportContactDTO;
import com.example.backend.domain.response.support.ResSupportIssueGuideDTO;
import com.example.backend.domain.response.support.ResSupportMaintenanceItemDTO;
import com.example.backend.domain.response.support.ResSupportResourceLinkDTO;
import com.example.backend.domain.entity.base.BaseTenantEntity;
import com.example.backend.repository.SupportContactRepository;
import com.example.backend.repository.SupportIssueGuideRepository;
import com.example.backend.repository.SupportMaintenanceItemRepository;
import com.example.backend.repository.SupportResourceLinkRepository;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportPageService {

    private final SupportContactRepository supportContactRepository;
    private final SupportIssueGuideRepository supportIssueGuideRepository;
    private final SupportResourceLinkRepository supportResourceLinkRepository;
    private final SupportMaintenanceItemRepository supportMaintenanceItemRepository;

    private long currentTenantId() {
        return TenantContext.requireCurrentTenantId();
    }

    private void assertSameTenant(BaseTenantEntity e) {
        if (e.getTenantId() == null || !e.getTenantId().equals(currentTenantId())) {
            throw new BadRequestException("Bản ghi không thuộc tenant hiện tại");
        }
    }

    // ─── Liên hệ ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ResSupportContactDTO> listContacts() {
        return supportContactRepository.findByTenantId(currentTenantId()).stream()
                .sorted(Comparator.comparing(SupportContact::getSortOrder).thenComparing(SupportContact::getId))
                .map(this::toResContact)
                .collect(Collectors.toList());
    }

    public ResSupportContactDTO createContact(ReqSupportContactDTO req) {
        SupportContact e = new SupportContact();
        applyContact(e, req);
        if (e.getSortOrder() == null) {
            e.setSortOrder(supportContactRepository.findMaxSortOrder(currentTenantId()) + 1);
        }
        return toResContact(supportContactRepository.save(e));
    }

    public ResSupportContactDTO updateContact(Long id, ReqSupportContactDTO req) throws IdInvalidException {
        SupportContact e = supportContactRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy liên hệ hỗ trợ"));
        assertSameTenant(e);
        applyContact(e, req);
        return toResContact(supportContactRepository.save(e));
    }

    public void deleteContact(Long id) throws IdInvalidException {
        SupportContact e = supportContactRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy liên hệ hỗ trợ"));
        assertSameTenant(e);
        supportContactRepository.delete(e);
    }

    private void applyContact(SupportContact e, ReqSupportContactDTO req) {
        e.setName(req.getName().trim());
        e.setRole(req.getRole().trim());
        e.setPhone(blankToNull(req.getPhone()));
        e.setEmail(blankToNull(req.getEmail()));
        e.setNote(blankToNull(req.getNote()));
        if (req.getSortOrder() != null) {
            e.setSortOrder(req.getSortOrder());
        }
    }

    private ResSupportContactDTO toResContact(SupportContact e) {
        return ResSupportContactDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .role(e.getRole())
                .phone(e.getPhone())
                .email(e.getEmail())
                .note(e.getNote())
                .sortOrder(e.getSortOrder())
                .build();
    }

    // ─── Hướng dẫn sự cố ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ResSupportIssueGuideDTO> listIssueGuides() {
        return supportIssueGuideRepository.findAllByTenantId(currentTenantId()).stream()
                .sorted(Comparator.comparing(SupportIssueGuide::getSortOrder).thenComparing(SupportIssueGuide::getId))
                .map(this::toResGuide)
                .collect(Collectors.toList());
    }

    public ResSupportIssueGuideDTO createIssueGuide(ReqSupportIssueGuideDTO req) {
        SupportIssueGuide e = new SupportIssueGuide();
        applyGuide(e, req);
        if (e.getSortOrder() == null) {
            e.setSortOrder(supportIssueGuideRepository.findMaxSortOrderForTenantId(currentTenantId()) + 1);
        }
        return toResGuide(supportIssueGuideRepository.save(e));
    }

    public ResSupportIssueGuideDTO updateIssueGuide(Long id, ReqSupportIssueGuideDTO req) throws IdInvalidException {
        SupportIssueGuide e = supportIssueGuideRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hướng dẫn sự cố"));
        assertSameTenant(e);
        applyGuide(e, req);
        return toResGuide(supportIssueGuideRepository.save(e));
    }

    public void deleteIssueGuide(Long id) throws IdInvalidException {
        SupportIssueGuide e = supportIssueGuideRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy hướng dẫn sự cố"));
        assertSameTenant(e);
        supportIssueGuideRepository.delete(e);
    }

    private void applyGuide(SupportIssueGuide e, ReqSupportIssueGuideDTO req) {
        e.setTitle(req.getTitle().trim());
        e.setSeverity(req.getSeverity());
        e.getSteps().clear();
        e.getSteps().addAll(req.getSteps().stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        if (e.getSteps().isEmpty()) {
            throw new BadRequestException("Cần ít nhất một bước xử lý không rỗng");
        }
        if (req.getSortOrder() != null) {
            e.setSortOrder(req.getSortOrder());
        }
    }

    private ResSupportIssueGuideDTO toResGuide(SupportIssueGuide e) {
        return ResSupportIssueGuideDTO.builder()
                .id(e.getId())
                .title(e.getTitle())
                .severity(e.getSeverity())
                .steps(List.copyOf(e.getSteps()))
                .sortOrder(e.getSortOrder())
                .build();
    }

    // ─── Link tài nguyên ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ResSupportResourceLinkDTO> listResourceLinks() {
        return supportResourceLinkRepository.findByTenantId(currentTenantId()).stream()
                .sorted(Comparator.comparing(SupportResourceLink::getSortOrder).thenComparing(SupportResourceLink::getId))
                .map(this::toResLink)
                .collect(Collectors.toList());
    }

    public ResSupportResourceLinkDTO createResourceLink(ReqSupportResourceLinkDTO req) {
        SupportResourceLink e = new SupportResourceLink();
        applyLink(e, req);
        if (e.getSortOrder() == null) {
            e.setSortOrder(supportResourceLinkRepository.findMaxSortOrder(currentTenantId()) + 1);
        }
        return toResLink(supportResourceLinkRepository.save(e));
    }

    public ResSupportResourceLinkDTO updateResourceLink(Long id, ReqSupportResourceLinkDTO req) throws IdInvalidException {
        SupportResourceLink e = supportResourceLinkRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy liên kết tài nguyên"));
        assertSameTenant(e);
        applyLink(e, req);
        return toResLink(supportResourceLinkRepository.save(e));
    }

    public void deleteResourceLink(Long id) throws IdInvalidException {
        SupportResourceLink e = supportResourceLinkRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy liên kết tài nguyên"));
        assertSameTenant(e);
        supportResourceLinkRepository.delete(e);
    }

    private void applyLink(SupportResourceLink e, ReqSupportResourceLinkDTO req) {
        e.setLabel(req.getLabel().trim());
        e.setUrl(req.getUrl().trim());
        e.setColor(blankToNull(req.getColor()));
        if (req.getSortOrder() != null) {
            e.setSortOrder(req.getSortOrder());
        }
    }

    private ResSupportResourceLinkDTO toResLink(SupportResourceLink e) {
        return ResSupportResourceLinkDTO.builder()
                .id(e.getId())
                .label(e.getLabel())
                .url(e.getUrl())
                .color(e.getColor())
                .sortOrder(e.getSortOrder())
                .build();
    }

    // ─── Ghi chú bảo trì ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ResSupportMaintenanceItemDTO> listMaintenanceItems() {
        return supportMaintenanceItemRepository.findByTenantId(currentTenantId()).stream()
                .sorted(Comparator.comparing(SupportMaintenanceItem::getSortOrder)
                        .thenComparing(SupportMaintenanceItem::getId))
                .map(this::toResMaint)
                .collect(Collectors.toList());
    }

    public ResSupportMaintenanceItemDTO createMaintenanceItem(ReqSupportMaintenanceItemDTO req) {
        SupportMaintenanceItem e = new SupportMaintenanceItem();
        applyMaint(e, req);
        if (e.getSortOrder() == null) {
            e.setSortOrder(supportMaintenanceItemRepository.findMaxSortOrder(currentTenantId()) + 1);
        }
        return toResMaint(supportMaintenanceItemRepository.save(e));
    }

    public ResSupportMaintenanceItemDTO updateMaintenanceItem(Long id, ReqSupportMaintenanceItemDTO req)
            throws IdInvalidException {
        SupportMaintenanceItem e = supportMaintenanceItemRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy mục bảo trì"));
        assertSameTenant(e);
        applyMaint(e, req);
        return toResMaint(supportMaintenanceItemRepository.save(e));
    }

    public void deleteMaintenanceItem(Long id) throws IdInvalidException {
        SupportMaintenanceItem e = supportMaintenanceItemRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy mục bảo trì"));
        assertSameTenant(e);
        supportMaintenanceItemRepository.delete(e);
    }

    private void applyMaint(SupportMaintenanceItem e, ReqSupportMaintenanceItemDTO req) {
        e.setLabel(req.getLabel().trim());
        e.setFrequencyText(req.getFrequencyText().trim());
        e.setColor(blankToNull(req.getColor()));
        if (req.getSortOrder() != null) {
            e.setSortOrder(req.getSortOrder());
        }
    }

    private ResSupportMaintenanceItemDTO toResMaint(SupportMaintenanceItem e) {
        return ResSupportMaintenanceItemDTO.builder()
                .id(e.getId())
                .label(e.getLabel())
                .frequencyText(e.getFrequencyText())
                .color(e.getColor())
                .sortOrder(e.getSortOrder())
                .build();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
