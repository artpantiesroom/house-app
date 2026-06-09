import { createContext, useContext, useMemo, useState } from 'react';
import { v4 as uuid } from 'uuid';
import {
  buildingContacts,
  buildingInfo,
  initialAnnouncements,
  initialMaintenanceRequests,
  initialPayments,
  initialResidents,
  sanitizeText,
  securityIncidents,
  simulateRequest,
} from '../data/mockData.js';

const DataContext = createContext(null);

export function DataProvider({ children }) {
  const [residents, setResidents] = useState(initialResidents);
  const [announcements, setAnnouncements] = useState(initialAnnouncements);
  const [requests, setRequests] = useState(initialMaintenanceRequests);
  const [payments] = useState(initialPayments);

  const loadPageData = (data, delay = 700) => simulateRequest(data, delay);
  const loadPayments = (data) => simulateRequest(data, 700);

  const addResident = async (resident) => {
    const next = { ...resident, id: uuid(), name: sanitizeText(resident.name), email: sanitizeText(resident.email), phone: sanitizeText(resident.phone), apartment: sanitizeText(resident.apartment), floor: Number(resident.floor) };
    await simulateRequest(next, 800);
    setResidents((current) => [next, ...current]);
    return next;
  };

  const editResident = async (id, resident) => {
    const next = { ...resident, id, name: sanitizeText(resident.name), email: sanitizeText(resident.email), phone: sanitizeText(resident.phone), apartment: sanitizeText(resident.apartment), floor: Number(resident.floor) };
    await simulateRequest(next, 800);
    setResidents((current) => current.map((item) => (item.id === id ? next : item)));
    return next;
  };

  const deleteResident = async (id) => {
    await simulateRequest(id, 800);
    setResidents((current) => current.filter((item) => item.id !== id));
  };

  const addAnnouncement = async (announcement) => {
    const next = { id: uuid(), title: sanitizeText(announcement.title), body: sanitizeText(announcement.body), date: new Date().toISOString().slice(0, 10), audience: 'All residents' };
    await simulateRequest(next, 800);
    setAnnouncements((current) => [next, ...current]);
    return next;
  };

  const editAnnouncement = async (id, announcement) => {
    const next = { ...announcement, id, title: sanitizeText(announcement.title), body: sanitizeText(announcement.body) };
    await simulateRequest(next, 800);
    setAnnouncements((current) => current.map((item) => (item.id === id ? next : item)));
    return next;
  };

  const deleteAnnouncement = async (id) => {
    await simulateRequest(id, 800);
    setAnnouncements((current) => current.filter((item) => item.id !== id));
  };

  const createRequest = async (request) => {
    const next = { id: uuid(), ...request, title: sanitizeText(request.title), category: sanitizeText(request.category), details: sanitizeText(request.details), status: 'Open', createdAt: new Date().toISOString() };
    await simulateRequest(next, 1000);
    setRequests((current) => [next, ...current]);
    return next;
  };

  const changeRequestStatus = async (id, status) => {
    await simulateRequest({ id, status }, 800);
    setRequests((current) => current.map((item) => (item.id === id ? { ...item, status } : item)));
  };

  const value = useMemo(() => ({
    residents,
    announcements,
    requests,
    payments,
    incidents: securityIncidents,
    contacts: buildingContacts,
    buildingInfo,
    loadPageData,
    loadPayments,
    addResident,
    editResident,
    deleteResident,
    addAnnouncement,
    editAnnouncement,
    deleteAnnouncement,
    createRequest,
    changeRequestStatus,
  }), [residents, announcements, requests, payments]);

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export const useData = () => useContext(DataContext);
