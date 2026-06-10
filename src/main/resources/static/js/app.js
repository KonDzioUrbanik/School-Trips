const API_BASE = '/api';

// Główny stan aplikacji
let jwtToken = localStorage.getItem('jwtToken') || null;
let currentUser = null; // { username, role }

const state = {
    wycieczki: [],
    klasy: [],
    uczniowie: [],
    nauczyciele: [],
    opiekunowie: [],
    uczestnictwa: [],
    zgody: [],
    currentFilter: 'wszystkie'
};

// Pomocnicza funkcja parsująca JWT w przeglądarce bez dodatkowych bibliotek
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Błąd dekodowania tokenu:', e);
        return null;
    }
}

// Funkcja fetchująca z automatycznym dołączaniem tokena JWT i obsługą błędów autoryzacji
async function fetchWithAuth(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    
    // Jeśli posiadamy token, dołączamy go w nagłówku
    if (jwtToken) {
        options.headers['Authorization'] = `Bearer ${jwtToken}`;
    }
    
    // Zapewniamy Content-Type dla żądań z body (jeśli nie jest to FormData)
    if (options.body && typeof options.body === 'string' && !options.headers['Content-Type']) {
        options.headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(url, options);
        
        if (response.status === 401) {
            // Token wygasł lub jest niepoprawny
            showAlert('error', 'Sesja wygasła. Zaloguj się ponownie.');
            logout(false); // wylogowanie lokalne
            throw new Error('Niezalogowany (401)');
        }
        
        if (response.status === 403) {
            showAlert('error', 'Brak uprawnień do wykonania tej operacji (403).');
            throw new Error('Brak uprawnień (403)');
        }

        return response;
    } catch (error) {
        console.error(`Błąd sieci dla ${url}:`, error);
        throw error;
    }
}

// Inicjalizacja przy załadowaniu strony
document.addEventListener('DOMContentLoaded', () => {
    // Sprawdzamy czy mamy ważny token
    if (jwtToken) {
        const payload = parseJwt(jwtToken);
        if (payload && payload.exp * 1000 > Date.now()) {
            currentUser = {
                username: payload.sub,
                role: payload.roles && payload.roles.length > 0 ? payload.roles[0] : 'ROLE_UCZEN_RODZIC'
            };
            initAppForUser();
        } else {
            // Wyczyszczenie wygasłego tokenu
            localStorage.removeItem('jwtToken');
            jwtToken = null;
            showAuthSection();
        }
    } else {
        showAuthSection();
    }
});

// Wyświetlenie sekcji logowania
function showAuthSection() {
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('dashboard-section').classList.add('hidden');
    document.getElementById('header-user-profile').classList.add('hidden');
}

// Szybkie logowanie (quick login helper)
function quickLogin(username, password) {
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
    document.getElementById('login-form').dispatchEvent(new Event('submit'));
}

// Obsługa wysłania formularza logowania
async function handleLogin(event) {
    event.preventDefault();
    
    const usernameInput = document.getElementById('login-username').value;
    const passwordInput = document.getElementById('login-password').value;
    
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading"></span> Logowanie...';

    // Próba wylogowania poprzedniej sesji w bazie danych za pomocą starego tokenu (jeśli istnieje),
    // aby zapobiec konfliktowi (409) przy tworzeniu nowego Refresh Tokenu na serwerze.
    if (jwtToken) {
        try {
            await fetchWithAuth('/api/auth/logout', { method: 'POST' });
        } catch (e) {
            console.warn('Nie udało się wyczyścić poprzedniej sesji na serwerze:', e);
        }
        localStorage.removeItem('jwtToken');
        jwtToken = null;
        currentUser = null;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });

        if (response.ok) {
            const data = await response.json();
            jwtToken = data.token;
            localStorage.setItem('jwtToken', jwtToken);
            
            const payload = parseJwt(jwtToken);
            currentUser = {
                username: payload.sub,
                role: payload.roles && payload.roles.length > 0 ? payload.roles[0] : 'ROLE_UCZEN_RODZIC'
            };
            
            showAlert('success', `Witaj z powrotem, ${currentUser.username}!`);
            initAppForUser();
        } else {
            if (response.status === 409) {
                showAlert('error', 'Wystąpił konflikt sesji w bazie (błąd 409). Spróbuj najpierw wylogować poprzednią sesję, zrestartować serwer lub wyczyścić tabelę refresh_token w bazie danych.');
            } else {
                const errData = await response.json().catch(() => ({}));
                showAlert('error', errData.message || 'Błędny login lub hasło.');
            }
        }
    } catch (error) {
        console.error('Błąd podczas logowania:', error);
        showAlert('error', 'Wystąpił błąd połączenia z serwerem.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Obsługa wylogowania
async function logout(notifyServer = true) {
    if (notifyServer && jwtToken) {
        try {
            await fetchWithAuth('/api/auth/logout', { method: 'POST' });
        } catch (e) {
            console.warn('Błąd wylogowania po stronie serwera:', e);
        }
    }
    
    // Czyszczenie stanu lokalnego
    jwtToken = null;
    currentUser = null;
    localStorage.removeItem('jwtToken');
    showAlert('info', 'Zostałeś pomyślnie wylogowany.');
    showAuthSection();
}

// Inicjalizacja aplikacji pod kątem uprawnień zalogowanego użytkownika
function initAppForUser() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('dashboard-section').classList.remove('hidden');
    
    // Konfiguracja nagłówka
    const headerProfile = document.getElementById('header-user-profile');
    headerProfile.classList.remove('hidden');
    document.getElementById('header-username').textContent = currentUser.username;
    
    let roleNamePl = 'Uczeń / Rodzic';
    if (currentUser.role === 'ROLE_ADMIN') roleNamePl = 'Administrator';
    else if (currentUser.role === 'ROLE_NAUCZYCIEL') roleNamePl = 'Nauczyciel';
    document.getElementById('header-user-role').textContent = roleNamePl;

    // Ukrywanie / pokazywanie elementów w zależności od roli (klasy CSS w HTML)
    const isAdmin = currentUser.role === 'ROLE_ADMIN';
    const isTeacher = currentUser.role === 'ROLE_NAUCZYCIEL';
    const isTeacherOrAdmin = isAdmin || isTeacher;

    // Selektory ról w HTML
    document.querySelectorAll('.role-teacher-admin').forEach(el => {
        el.classList.toggle('hidden', !isTeacherOrAdmin);
    });
    document.querySelectorAll('.role-admin').forEach(el => {
        el.classList.toggle('hidden', !isAdmin);
    });

    // Ukrywanie statystyk dla uchodźców/uczniów
    document.getElementById('stats-container').classList.toggle('hidden', !isTeacherOrAdmin);

    // Dynamiczna generacja zakładek nawigacji
    const navTabsContainer = document.getElementById('role-nav-tabs');
    let tabsHtml = '';

    if (isTeacherOrAdmin) {
        tabsHtml = `
            <button class="nav-tab active" data-section="wycieczki-tab-section" onclick="switchTab('wycieczki-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                Wycieczki
            </button>
            <button class="nav-tab" data-section="uczniowie-tab-section" onclick="switchTab('uczniowie-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                Uczniowie
            </button>
            <button class="nav-tab" data-section="nauczyciele-tab-section" onclick="switchTab('nauczyciele-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                Nauczyciele
            </button>
            <button class="nav-tab" data-section="opiekunowie-tab-section" onclick="switchTab('opiekunowie-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12a3 3 0 11-6 0 3 3 0 016 0zm3 2a9 9 0 00-9 9h18a9 9 0 00-9-9zm9-3a3 3 0 11-6 0 3 3 0 016 0zm-9 2a9 9 0 00-9 9h18a9 9 0 00-9-9z"></path></svg>
                Opiekunowie
            </button>
        `;

        if (isTeacherOrAdmin) {
            tabsHtml += `
                <button class="nav-tab" data-section="klasy-tab-section" onclick="switchTab('klasy-tab-section')">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path></svg>
                    Klasy
                </button>
            `;
        }

        tabsHtml += `
            <button class="nav-tab" data-section="zapisy-tab-section" onclick="switchTab('zapisy-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
                Zapisy i Zgody
            </button>
        `;
    } else {
        tabsHtml = `
            <button class="nav-tab active" data-section="wycieczki-tab-section" onclick="switchTab('wycieczki-tab-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                Wycieczki
            </button>
            <button class="nav-tab" data-section="zapis-wycieczka-section" onclick="switchTab('zapis-wycieczka-section')">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
                Zapisz się
            </button>
        `;
    }

    navTabsContainer.innerHTML = tabsHtml;

    // Reset aktywnej sekcji na Wycieczki
    switchTab('wycieczki-tab-section');

    // Załadowanie pierwszych danych
    loadInitialData();
}

// Przełączanie zakładek
function switchTab(sectionId) {
    document.querySelectorAll('.section').forEach(sec => sec.classList.remove('active'));
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.section === sectionId);
    });
    
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

// Pobieranie wszystkich danych z backendu
async function loadInitialData() {
    try {
        await Promise.allSettled([
            loadWycieczki(),
            loadKlasy(),
            loadUczniowie(),
            loadNauczyciele(),
            loadOpiekunowie(),
            loadParticipations(),
            loadConsents()
        ]);
        
        calculateStatsAndPopulate();
        renderWycieczki(state.wycieczki);
    } catch (error) {
        console.error('Błąd podczas pobierania danych początkowych:', error);
    }
}

// Wyliczenie statystyk na pulpicie
function calculateStatsAndPopulate() {
    document.getElementById('stat-trips-count').textContent = state.wycieczki.length;
    document.getElementById('stat-students-count').textContent = state.uczniowie.length;
    
    const deliveredConsents = state.zgody.filter(z => z.czyDostarczona).length;
    document.getElementById('stat-consents-count').textContent = deliveredConsents;
}

// ================= POBIERANIE ZASOBÓW Z API =================

async function loadWycieczki() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/wycieczka`);
        if (res.ok) {
            state.wycieczki = await res.json();
            renderWycieczki(state.wycieczki);
            
            // Populate teacher/admin enrollment select with closed warning if applicable
            populateSelectOptions('enroll-trip', state.wycieczki, w => {
                const dataStart = w.data_rozpoczecia || w.dataRozpoczecia;
                let suffix = '';
                if (dataStart) {
                    const startDate = new Date(dataStart);
                    const today = new Date();
                    startDate.setHours(0,0,0,0);
                    today.setHours(0,0,0,0);
                    const diffDays = Math.ceil((startDate.getTime() - today.getTime()) / (1000 * 3600 * 24));
                    if (diffDays < 7 || w.status !== 'PLANOWANA') {
                        suffix = ' (Zapisy zamknięte)';
                    }
                }
                return `${w.nazwa} (${w.miejsce_docelowe || w.miejsceDocelowe})${suffix}`;
            });

            // Populate student enrollment select (only trips open for enrollment)
            const openTripsForStudents = state.wycieczki.filter(w => {
                if (w.status !== 'PLANOWANA') return false;
                const dataStart = w.data_rozpoczecia || w.dataRozpoczecia;
                if (!dataStart) return false;
                const startDate = new Date(dataStart);
                const today = new Date();
                startDate.setHours(0,0,0,0);
                today.setHours(0,0,0,0);
                const diffDays = Math.ceil((startDate.getTime() - today.getTime()) / (1000 * 3600 * 24));
                return diffDays >= 7;
            });
            populateSelectOptions('student-direct-enroll-trip', openTripsForStudents, w => `${w.nazwa} (${w.miejsce_docelowe || w.miejsceDocelowe})`);
            
            populateSelectOptions('guide-trip', state.wycieczki, w => w.nazwa);
        }
    } catch (e) {
        console.error('Błąd loadWycieczki:', e);
    }
}

async function loadKlasy() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/klasy`);
        if (res.ok) {
            state.klasy = await res.json();
            renderClasses(state.klasy);
            populateSelectOptions('student-class', state.klasy, k => `${k.nazwa} (${k.profil})`);
        }
    } catch (e) {
        console.error('Błąd loadKlasy:', e);
    }
}

async function loadUczniowie() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/uczen`);
        if (res.ok) {
            const rawData = await res.json();
            state.uczniowie = rawData.map(u => ({
                ...u,
                klasaId: u.klasa ? u.klasa.id : null
            }));
            renderStudents(state.uczniowie);
            populateSelectOptions('enroll-student', state.uczniowie, u => {
                const klasa = state.klasy.find(k => k.id === u.klasaId);
                const klasaName = klasa ? klasa.nazwa : (u.klasaId ? `Klasa ${u.klasaId}` : 'Brak');
                return `${u.imie} ${u.nazwisko} (${klasaName})`;
            });
            
            if (currentUser && currentUser.role === 'ROLE_UCZEN_RODZIC') {
                try {
                    const meRes = await fetchWithAuth(`${API_BASE}/uczen/me`);
                    if (meRes.ok) {
                        const myProfile = await meRes.json();
                        currentUser.studentId = myProfile.id;
                        const selectEl = document.getElementById('student-direct-enroll-uczen');
                        if (selectEl) {
                            selectEl.innerHTML = `<option value="${myProfile.id}" selected>${myProfile.imie} ${myProfile.nazwisko}</option>`;
                            const group = selectEl.closest('.form-group');
                            if (group) group.style.display = 'none';
                        }
                    }
                } catch (err) {
                    console.error('Błąd pobierania profilu ucznia:', err);
                }
            } else {
                populateSelectOptions('student-direct-enroll-uczen', state.uczniowie, u => {
                    const klasa = state.klasy.find(k => k.id === u.klasaId);
                    const klasaName = klasa ? klasa.nazwa : (u.klasaId ? `Klasa ${u.klasaId}` : 'Brak');
                    return `${u.imie} ${u.nazwisko} (${klasaName})`;
                });
                const selectEl = document.getElementById('student-direct-enroll-uczen');
                if (selectEl) {
                    const group = selectEl.closest('.form-group');
                    if (group) group.style.display = 'block';
                }
            }
        }
    } catch (e) {
        console.error('Błąd loadUczniowie:', e);
    }
}

async function loadNauczyciele() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/nauczyciel`);
        if (res.ok) {
            state.nauczyciele = await res.json();
            renderTeachers(state.nauczyciele);
            populateSelectOptions('guide-teacher', state.nauczyciele, n => `${n.imie} ${n.nazwisko} (${n.przedmiot})`);
        }
    } catch (e) {
        console.error('Błąd loadNauczyciele:', e);
    }
}

async function loadOpiekunowie() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/opiekun_wycieczki`);
        if (res.ok) {
            const rawData = await res.json();
            state.opiekunowie = rawData.map(g => ({
                id: g.id,
                rola: g.rola,
                wycieczkaId: g.wycieczkaId || (g.wycieczkaOpiekun ? g.wycieczkaOpiekun.id : null),
                nauczycielId: g.nauczycielId || (g.nauczyciel ? g.nauczyciel.id : null)
            }));
            renderGuides(state.opiekunowie);
        }
    } catch (e) {
        console.error('Błąd loadOpiekunowie:', e);
    }
}

async function loadParticipations() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/uczestnictwo`);
        if (res.ok) {
            const rawData = await res.json();
            state.uczestnictwa = rawData.map(p => ({
                id: p.id,
                uczenId: p.uczenId || (p.uczen ? p.uczen.id : null),
                wycieczkaId: p.wycieczkaId || (p.wycieczka ? p.wycieczka.id : null),
                czyJedzie: p.czyJedzie !== undefined ? p.czyJedzie : (p.czy_jedzie !== undefined ? p.czy_jedzie : false),
                uwagi: p.uwagi,
                dataZapisania: p.dataZapisania || p.data_zapisania
            }));
            renderParticipations();
        }
    } catch (e) {
        console.error('Błąd loadParticipations:', e);
    }
}

async function loadConsents() {
    try {
        const res = await fetchWithAuth(`${API_BASE}/zgoda_rodzica`);
        if (res.ok) {
            const rawData = await res.json();
            state.zgody = rawData.map(z => ({
                id: z.id,
                uczestnictwoId: z.uczestnictwoId || (z.uczestnictwo ? z.uczestnictwo.id : null),
                forma: z.forma,
                czyDostarczona: z.czyDostarczona !== undefined ? z.czyDostarczona : (z.czy_dostarczona !== undefined ? z.czy_dostarczona : false),
                dataPodpisania: z.dataPodpisania || z.data_podpisania
            }));
            renderParticipations(); // Przeładuj widok zapisów, ponieważ zgody są tam wyświetlane
        }
    } catch (e) {
        console.error('Błąd loadConsents:', e);
    }
}

// Pomocnicza funkcja do wypełniania selectów
function populateSelectOptions(selectId, items, labelFn) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    const firstOption = select.options[0] ? select.options[0].outerHTML : '<option value="">Wybierz</option>';
    
    select.innerHTML = firstOption + items.map(item => 
        `<option value="${item.id}">${labelFn(item)}</option>`
    ).join('');
}

// ================= RENDEROWANIE WIDOKÓW / TABEL =================

function renderWycieczki(wycieczki) {
    const container = document.getElementById('trips-grid-container');
    if (!container) return;

    if (wycieczki.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                <h3>Brak dostępnych wycieczek</h3>
                <p>Obecnie w systemie nie ma żadnych wycieczek.</p>
            </div>
        `;
        return;
    }

    const isTeacherOrAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL';

    container.innerHTML = wycieczki.map(w => {
        const statusClass = `status-${(w.status || 'PLANOWANA').toLowerCase()}`;
        const statusNamePl = w.status === 'PLANOWANA' ? 'Planowana' : (w.status === 'W_TRAKCIE' ? 'W trakcie' : 'Zakończona');
        const miejsce = w.miejsce_docelowe || w.miejsceDocelowe || '';
        const dataStart = w.data_rozpoczecia || w.dataRozpoczecia || '';
        const dataEnd = w.data_zakonczenia || w.dataZakonczenia || '';
        const koszt = w.koszt_na_osobe || w.kosztNaOsobe || 0;
        const zaliczka = (koszt * 0.20).toFixed(2);

        // Check if registration is closed (less than 7 days before trip start date)
        let isClosed = false;
        if (dataStart) {
            const startDate = new Date(dataStart);
            const today = new Date();
            startDate.setHours(0,0,0,0);
            today.setHours(0,0,0,0);
            const diffDays = Math.ceil((startDate.getTime() - today.getTime()) / (1000 * 3600 * 24));
            if (diffDays < 7) {
                isClosed = true;
            }
        }
        
        return `
            <div class="trip-card">
                <div class="trip-banner">
                    <div class="trip-banner-overlay"></div>
                    <div class="trip-title-wrapper">
                        <h3>${w.nazwa}</h3>
                        <div class="trip-destination">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                            <span>${miejsce}</span>
                        </div>
                    </div>
                </div>
                <div class="trip-body">
                    <div class="trip-details">
                        <div class="trip-detail-item">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                            <span>${formatDate(dataStart)}</span>
                        </div>
                        <div class="trip-detail-item">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 11l7-7 7 7M5 19l7-7 7 7"></path></svg>
                            <span>Do: ${formatDate(dataEnd)}</span>
                        </div>
                        <div class="trip-detail-item">
                            <span class="status-badge ${statusClass}">${statusNamePl}</span>
                        </div>
                        <div class="trip-detail-item" style="color: #eab308; font-weight: 600;">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="color: #eab308;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            <span>Zaliczka: ${zaliczka} PLN</span>
                        </div>
                    </div>
                    
                    <div class="trip-cost-row">
                        <div class="trip-price">
                            ${koszt} <span>PLN / os</span>
                        </div>
                        <div style="display: flex; gap: 8px; align-items: center;">
                            <button class="btn btn-secondary btn-sm" onclick="openTripDetails(${w.id})" title="Szczegóły i Plan Wycieczki">
                                Szczegóły 📋
                            </button>
                            ${isTeacherOrAdmin ? `
                                <div class="actions-cell">
                                    <button class="btn btn-secondary btn-sm btn-icon" onclick="openTripModal(${w.id})" title="Edytuj">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                                    </button>
                                    <button class="btn btn-danger btn-sm btn-icon" onclick="deleteTrip(${w.id})" title="Usuń">
                                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                                    </button>
                                </div>
                            ` : (
                                currentUser.role === 'ROLE_UCZEN_RODZIC' && currentUser.studentId && 
                                (state.uczestnictwa || []).some(p => p.wycieczkaId === w.id && p.uczenId === currentUser.studentId)
                            ) ? `
                                <span class="status-badge status-zakonczona" style="font-size:0.75rem; padding: 6px 12px; margin: 0;">Zapisany(a) ✓</span>
                            ` : isClosed ? `
                                <span class="status-badge status-planowana" style="font-size:0.75rem; padding: 6px 12px; margin: 0; background: rgba(239, 68, 68, 0.15); color: #f87171;">Zapisy zamknięte</span>
                            ` : `
                                <button class="btn btn-primary btn-sm" onclick="enrollInTripFromCard(${w.id})">
                                    Zapisz się
                                </button>
                            `}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function filterWycieczki(status) {
    state.currentFilter = status;
    const buttons = document.querySelectorAll('#trip-filters .filter-btn');
    buttons.forEach(btn => btn.classList.toggle('active', btn.dataset.filter === status));
    
    if (status === 'wszystkie') {
        renderWycieczki(state.wycieczki);
    } else {
        const filtered = state.wycieczki.filter(w => (w.status || '').toUpperCase() === status.toUpperCase());
        renderWycieczki(filtered);
    }
}

function renderStudents(students) {
    const tbody = document.getElementById('students-table-body');
    if (!tbody) return;

    if (students.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center">Brak zarejestrowanych uczniów.</td></tr>`;
        return;
    }

    const isTeacherOrAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL';

    tbody.innerHTML = students.map(s => {
        const klasa = state.klasy.find(k => k.id === s.klasaId);
        const klasaName = klasa ? `${klasa.nazwa} (${klasa.profil})` : `ID: ${s.klasaId}`;
        
        return `
            <tr>
                <td style="font-weight: 600;">${s.imie} ${s.nazwisko}</td>
                <td>${klasaName}</td>
                <td>${formatDate(s.data_urodzenia)}</td>
                ${isTeacherOrAdmin ? `
                    <td>
                        <div class="actions-cell">
                            <button class="btn btn-secondary btn-sm btn-icon" onclick="openStudentModal(${s.id})" title="Edytuj">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                            </button>
                            <button class="btn btn-danger btn-sm btn-icon" onclick="deleteStudent(${s.id})" title="Usuń">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </button>
                        </div>
                    </td>
                ` : ''}
            </tr>
        `;
    }).join('');
    
    // Ustawienie widoczności kolumny "Akcje"
    const headerActionCol = document.querySelector('#students-tab-section th.role-teacher-admin');
    if (headerActionCol) {
        headerActionCol.classList.toggle('hidden', !isTeacherOrAdmin);
    }
}

function renderClasses(classes) {
    const tbody = document.getElementById('classes-table-body');
    if (!tbody) return;

    if (classes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="text-center">Brak zdefiniowanych klas.</td></tr>`;
        return;
    }

    tbody.innerHTML = classes.map(k => `
        <tr>
            <td style="font-weight: 500; color: var(--text-secondary);">#${k.id}</td>
            <td style="font-weight: 600; font-size:1.1rem; color: var(--primary);">${k.nazwa}</td>
            <td>${k.profil || '<span class="text-muted">brak</span>'}</td>
            <td>
                <div class="actions-cell">
                    <button class="btn btn-secondary btn-sm btn-icon" onclick="openClassModal(${k.id})" title="Edytuj">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                    </button>
                    <button class="btn btn-danger btn-sm btn-icon" onclick="deleteClass(${k.id}, '${k.nazwa}', '${k.profil}')" title="Usuń">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderTeachers(teachers) {
    const tbody = document.getElementById('teachers-table-body');
    if (!tbody) return;

    if (teachers.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center">Brak nauczycieli.</td></tr>`;
        return;
    }

    const isAdmin = currentUser.role === 'ROLE_ADMIN';

    tbody.innerHTML = teachers.map(n => `
        <tr>
            <td style="font-weight: 600;">${n.imie} ${n.nazwisko}</td>
            <td><span class="user-role" style="background:rgba(99,102,241,0.1); color:#a5b4fc;">${n.przedmiot}</span></td>
            <td>${n.telefon_kontaktowy}</td>
            ${isAdmin ? `
                <td>
                    <div class="actions-cell">
                        <button class="btn btn-secondary btn-sm btn-icon" onclick="openTeacherModal(${n.id})" title="Edytuj">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                        </button>
                        <button class="btn btn-danger btn-sm btn-icon" onclick="deleteTeacher(${n.id}, '${n.imie}', '${n.nazwisko}', '${n.przedmiot}', '${n.telefon_kontaktowy}')" title="Usuń">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </div>
                </td>
            ` : ''}
        </tr>
    `).join('');
    
    const headerActionCol = document.querySelector('#nauczyciele-tab-section th.role-admin');
    if (headerActionCol) {
        headerActionCol.classList.toggle('hidden', !isAdmin);
    }
}

function renderGuides(guides) {
    const tbody = document.getElementById('guides-table-body');
    if (!tbody) return;

    if (guides.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-center">Brak przypisanych opiekunów.</td></tr>`;
        return;
    }

    const isTeacherOrAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL';

    tbody.innerHTML = guides.map(g => {
        const wycieczka = state.wycieczki.find(w => w.id === g.wycieczkaId);
        const nauczyciel = state.nauczyciele.find(n => n.id === g.nauczycielId);
        
        const tripName = wycieczka ? wycieczka.nazwa : `Wycieczka (ID: ${g.wycieczkaId})`;
        const teacherName = nauczyciel ? `${nauczyciel.imie} ${nauczyciel.nazwisko}` : `Nauczyciel (ID: ${g.nauczycielId})`;
        
        return `
            <tr>
                <td style="font-weight: 600; color:var(--primary);">${tripName}</td>
                <td>${teacherName}</td>
                <td><span class="status-badge status-w_trakcie" style="font-size:0.75rem;">${g.rola}</span></td>
                ${isTeacherOrAdmin ? `
                    <td>
                        <div class="actions-cell">
                            <button class="btn btn-secondary btn-sm btn-icon" onclick="openGuideModal(${g.id})" title="Edytuj">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                            </button>
                            <button class="btn btn-danger btn-sm btn-icon" onclick="deleteGuide(${g.id})" title="Usuń">
                                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </button>
                        </div>
                    </td>
                ` : ''}
            </tr>
        `;
    }).join('');
    
    const headerActionCol = document.querySelector('#opiekunowie-tab-section th.role-teacher-admin');
    if (headerActionCol) {
        headerActionCol.classList.toggle('hidden', !isTeacherOrAdmin);
    }
}

function renderParticipations() {
    const tbody = document.getElementById('participations-table-body');
    if (!tbody) return;

    if (state.uczestnictwa.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center">Brak zapisów na wycieczki.</td></tr>`;
        return;
    }

    const isTeacherOrAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL';

    tbody.innerHTML = state.uczestnictwa.map(part => {
        const student = state.uczniowie.find(u => u.id === part.uczenId);
        const wycieczka = state.wycieczki.find(w => w.id === part.wycieczkaId);
        const consent = state.zgody.find(z => z.uczestnictwoId === part.id);
        
        const studentName = student ? `${student.imie} ${student.nazwisko}` : `Uczeń (ID: ${part.uczenId})`;
        const tripName = wycieczka ? wycieczka.nazwa : `Wycieczka (ID: ${part.wycieczkaId})`;
        
        const isGoingText = part.czyJedzie 
            ? '<span class="status-badge status-zakonczona" style="font-size:0.75rem;">Jedzie</span>' 
            : '<span class="status-badge status-planowana" style="font-size:0.75rem;">Nie jedzie</span>';
            
        let consentBadge = '<span class="status-badge status-planowana" style="font-size:0.7rem;">Brak Zgody</span>';
        let consentInfo = '-';
        let pdfAction = '';
        let consentAction = '';

        if (consent) {
            const dateStr = formatDate(consent.dataPodpisania);
            const formaText = consent.forma === 'ELEKTRONICZNA' ? 'Elektr.' : 'Papier.';
            
            if (consent.czyDostarczona) {
                consentBadge = '<span class="status-badge status-zakonczona" style="font-size:0.7rem;">Dostarczona</span>';
                consentInfo = `${formaText} (${dateStr})`;
                pdfAction = `
                    <button class="btn btn-success btn-sm btn-icon" onclick="downloadConsentPdf(${consent.id})" title="Pobierz PDF Zgody">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
                    </button>
                `;
            } else {
                consentBadge = '<span class="status-badge status-planowana" style="font-size:0.7rem;">Niedostarczona</span>';
                consentInfo = `${formaText} (Oczekuje)`;
            }

            if (isTeacherOrAdmin) {
                consentAction = `
                    <button class="btn btn-secondary btn-sm" onclick="openConsentModal(${part.id}, ${consent.id})">
                        Edytuj Zgodę
                    </button>
                `;
            }
        } else {
            // Zgoda nie istnieje w ogóle w bazie
            consentAction = `
                <button class="btn btn-primary btn-sm btn-success" onclick="openConsentModal(${part.id}, null)">
                    Dodaj Zgodę
                </button>
            `;
        }

        const deleteAction = isTeacherOrAdmin ? `
            <button class="btn btn-danger btn-sm btn-icon" onclick="deleteParticipation(${part.id})" title="Wycofaj Zapis">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width:16px;height:16px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
            </button>
        ` : '';

        return `
            <tr>
                <td style="font-weight:600;">${studentName}</td>
                <td style="color:var(--primary); font-weight:500;">${tripName}</td>
                <td>${isGoingText}</td>
                <td style="font-size: 0.85rem; color:var(--text-secondary); max-width:150px; overflow:hidden; text-overflow:ellipsis;">${part.uwagi || '-'}</td>
                <td>${consentBadge}</td>
                <td style="font-size:0.85rem;">${consentInfo}</td>
                <td>
                    <div class="actions-cell">
                        ${pdfAction}
                        ${consentAction}
                        ${deleteAction}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// ================= MODAL CONTROLLER =================

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
    
    // Wyczyszczenie formularza wewnątrz modala
    const form = document.querySelector(`#${modalId} form`);
    if (form) form.reset();
}

// --- TRIPS ---
function openTripModal(tripId = null) {
    const title = document.getElementById('trip-modal-title');
    const form = document.getElementById('trip-form');
    
    if (tripId) {
        title.textContent = 'Edytuj Wycieczkę';
        const trip = state.wycieczki.find(w => w.id === tripId);
        if (trip) {
            document.getElementById('trip-id').value = trip.id;
            document.getElementById('trip-name').value = trip.nazwa;
            document.getElementById('trip-destination').value = trip.miejsceDocelowe;
            document.getElementById('trip-start-date').value = trip.dataRozpoczecia;
            document.getElementById('trip-end-date').value = trip.dataZakonczenia;
            document.getElementById('trip-cost').value = trip.kosztNaOsobe;
            document.getElementById('trip-status').value = trip.status || 'PLANOWANA';
        }
    } else {
        title.textContent = 'Dodaj Nową Wycieczkę';
        form.reset();
        document.getElementById('trip-id').value = '';
        document.getElementById('trip-status').value = 'PLANOWANA';
        
        // Domyślna data rozpoczęcia na jutro
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('trip-start-date').value = tomorrow.toISOString().split('T')[0];
        
        const dayAfter = new Date();
        dayAfter.setDate(tomorrow.getDate() + 2);
        document.getElementById('trip-end-date').value = dayAfter.toISOString().split('T')[0];
    }
    
    openModal('trip-modal');
}

async function submitTripForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('trip-id').value;
    const bodyData = {
        nazwa: document.getElementById('trip-name').value,
        miejsceDocelowe: document.getElementById('trip-destination').value,
        dataRozpoczecia: document.getElementById('trip-start-date').value,
        dataZakonczenia: document.getElementById('trip-end-date').value,
        kosztNaOsobe: parseFloat(document.getElementById('trip-cost').value),
        status: document.getElementById('trip-status').value
    };

    try {
        let response;
        if (id) {
            // Edycja
            response = await fetchWithAuth(`${API_BASE}/wycieczka/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
        } else {
            // Dodawanie
            response = await fetchWithAuth(`${API_BASE}/wycieczka`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', id ? 'Zaktualizowano wycieczkę!' : 'Dodano nową wycieczkę!');
            closeModal('trip-modal');
            await loadWycieczki();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd podczas zapisywania wycieczki.');
        }
    } catch (e) {
        showAlert('error', 'Wystąpił błąd komunikacji z serwerem.');
    }
}

async function deleteTrip(id) {
    if (!confirm('Czy na pewno chcesz usunąć tę wycieczkę? Spowoduje to usunięcie powiązanych opiekunów i zgłoszeń!')) return;
    
    try {
        const response = await fetchWithAuth(`${API_BASE}/wycieczka/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('success', 'Usunięto wycieczkę.');
            await loadWycieczki();
            await loadOpiekunowie();
            await loadParticipations();
        } else {
            showAlert('error', 'Nie udało się usunąć wycieczki.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia z serwerem.');
    }
}

// --- AI TRIP PLAN DETAILS ---
let selectedTripForDetailsId = null;

async function openTripDetails(tripId) {
    selectedTripForDetailsId = tripId;
    const trip = state.wycieczki.find(w => w.id === tripId);
    if (!trip) return;

    // Ustawienie danych podstawowych w modalu szczegółów
    document.getElementById('trip-details-title').textContent = `Wycieczka: ${trip.nazwa}`;
    document.getElementById('trip-details-destination').textContent = trip.miejsce_docelowe || trip.miejsceDocelowe || 'Brak';
    
    const dataStart = trip.data_rozpoczecia || trip.dataRozpoczecia || '';
    const dataEnd = trip.data_zakonczenia || trip.dataZakonczenia || '';
    document.getElementById('trip-details-dates').textContent = `${formatDate(dataStart)} - ${formatDate(dataEnd)}`;
    
    const koszt = trip.koszt_na_osobe || trip.kosztNaOsobe || 0;
    document.getElementById('trip-details-cost').textContent = `${koszt} PLN`;

    // Wyliczenie i wyświetlenie zaliczki 20%
    const advance = (koszt * 0.20).toFixed(2);
    document.getElementById('trip-details-advance').textContent = `${advance} PLN`;

    // Renderowanie planu wycieczki (Markdown -> HTML za pomocą marked)
    const planContentDiv = document.getElementById('trip-plan-content');
    const planText = trip.planWycieczki;
    
    if (planText && planText.trim()) {
        planContentDiv.innerHTML = marked.parse(planText);
        document.getElementById('btn-generate-ai-plan').textContent = 'Regeneruj Plan przez AI 🤖';
    } else {
        planContentDiv.innerHTML = `<div class="text-center text-muted" style="padding: 40px 0;">
            <p>Brak planu wycieczki.</p>
            ${(currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL') 
                ? '<p style="font-size: 0.9rem;">Kliknij przycisk powyżej, aby wygenerować plan za pomocą AI.</p>' 
                : '<p style="font-size: 0.9rem;">Poproś opiekuna o wygenerowanie planu wycieczki.</p>'}
        </div>`;
        document.getElementById('btn-generate-ai-plan').textContent = 'Generuj Plan przez AI 🤖';
    }

    // Ukrywanie / Pokazywanie przycisku generowania AI na podstawie roli
    const isTeacherOrAdmin = currentUser.role === 'ROLE_ADMIN' || currentUser.role === 'ROLE_NAUCZYCIEL';
    const generateBtn = document.getElementById('btn-generate-ai-plan');
    if (generateBtn) {
        generateBtn.classList.toggle('hidden', !isTeacherOrAdmin);
    }

    // Ukrywanie / Pokazywanie przycisku pobierania PDF (tylko dla wycieczek PLANOWANA)
    const downloadPdfBtn = document.getElementById('btn-download-pdf');
    if (downloadPdfBtn) {
        downloadPdfBtn.classList.toggle('hidden', !isTeacherOrAdmin || (trip.status || 'PLANOWANA') !== 'PLANOWANA');
    }

    // Pobranie i wyrenderowanie listy uczestników wycieczki
    const partTbody = document.getElementById('trip-participants-tbody');
    const partContainer = document.getElementById('trip-participants-container');
    
    if (partTbody && partContainer) {
        if (isTeacherOrAdmin) {
            partContainer.classList.remove('hidden');
            
            // Filtrujemy zapisy dotyczące tej konkretnej wycieczki
            const tripParts = (state.uczestnictwa || []).filter(p => p.wycieczkaId === tripId);
            
            if (tripParts.length === 0) {
                partTbody.innerHTML = `<tr><td colspan="4" class="text-center" style="padding: 20px 0; color: var(--text-muted);">Brak zapisanych uczestników na tę wycieczkę.</td></tr>`;
            } else {
                partTbody.innerHTML = tripParts.map(part => {
                    const student = (state.uczniowie || []).find(u => u.id === part.uczenId);
                    const studentName = student ? `${student.imie} ${student.nazwisko}` : `Uczeń (ID: ${part.uczenId})`;
                    const classId = student ? student.klasaId : null;
                    const klasa = classId ? (state.klasy || []).find(k => k.id === classId) : null;
                    const klasaName = klasa ? ` (${klasa.nazwa})` : '';
                    
                    const isGoingText = part.czyJedzie 
                        ? '<span class="status-badge status-zakonczona" style="font-size:0.75rem;">Jedzie</span>' 
                        : '<span class="status-badge status-planowana" style="font-size:0.75rem;">Nie jedzie</span>';
                        
                    const consent = (state.zgody || []).find(z => z.uczestnictwoId === part.id);
                    let consentBadge = '<span class="status-badge status-planowana" style="font-size:0.7rem;">Brak Zgody</span>';
                    if (consent) {
                        if (consent.czyDostarczona) {
                            consentBadge = `<span class="status-badge status-zakonczona" style="font-size:0.7rem;">Dostarczona (${consent.forma === 'ELEKTRONICZNA' ? 'Elektr.' : 'Papier.'})</span>`;
                        } else {
                            consentBadge = `<span class="status-badge status-planowana" style="font-size:0.7rem;">Niedostarczona (${consent.forma === 'ELEKTRONICZNA' ? 'Elektr.' : 'Papier.'})</span>`;
                        }
                    }
                    
                    const notes = part.uwagi ? part.uwagi : '<span class="text-muted">-</span>';
                    
                    return `
                        <tr>
                            <td><strong style="color: var(--text-primary);">${studentName}</strong>${klasaName}</td>
                            <td>${isGoingText}</td>
                            <td>${consentBadge}</td>
                            <td style="font-size: 0.9rem; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${part.uwagi || ''}">${notes}</td>
                        </tr>
                    `;
                }).join('');
            }
        } else {
            partContainer.classList.add('hidden');
        }
    }

    openModal('trip-details-modal');
}

async function generateAiPlan() {
    if (!selectedTripForDetailsId) return;
    
    const generateBtn = document.getElementById('btn-generate-ai-plan');
    const planContentDiv = document.getElementById('trip-plan-content');
    
    const originalText = generateBtn.textContent;
    generateBtn.disabled = true;
    generateBtn.innerHTML = '<span class="loading"></span> Generowanie...';
    planContentDiv.innerHTML = `<div class="text-center" style="padding: 40px 0;">
        <span class="loading" style="width: 30px; height: 30px; border-width: 3px; margin-bottom: 12px;"></span>
        <p style="color: var(--primary); font-weight: 600;">Sztuczna inteligencja tworzy plan podróży...</p>
        <p style="font-size: 0.85rem; color: var(--text-secondary);">Może to potrwać kilka sekund.</p>
    </div>`;

    try {
        const response = await fetchWithAuth(`${API_BASE}/wycieczka/${selectedTripForDetailsId}/generuj-plan`, {
            method: 'POST'
        });

        if (response.ok) {
            const updatedTrip = await response.json();
            
            // Zaktualizuj stan lokalny wycieczek
            const index = state.wycieczki.findIndex(w => w.id === updatedTrip.id);
            if (index !== -1) {
                state.wycieczki[index] = updatedTrip;
            }
            
            // Odśwież listę na widoku głównym
            renderWycieczki(state.wycieczki);
            
            // Wyświetl nowy plan w modalu szczegółów
            planContentDiv.innerHTML = marked.parse(updatedTrip.planWycieczki);
            generateBtn.textContent = 'Regeneruj Plan przez AI 🤖';
            showAlert('success', 'Plan wycieczki został pomyślnie wygenerowany przez AI!');
        } else {
            const err = await response.json().catch(() => ({}));
            planContentDiv.innerHTML = `<div class="text-center text-danger" style="padding: 40px 0;">
                <p>Wystąpił błąd podczas generowania planu.</p>
                <p style="font-size: 0.85rem;">${err.message || 'Nieznany błąd serwera'}</p>
            </div>`;
            generateBtn.textContent = originalText;
            showAlert('error', err.message || 'Błąd podczas generowania planu przez AI.');
        }
    } catch (e) {
        planContentDiv.innerHTML = `<div class="text-center text-danger" style="padding: 40px 0;">
            <p>Błąd połączenia z serwerem.</p>
        </div>`;
        generateBtn.textContent = originalText;
        showAlert('error', 'Błąd komunikacji z serwerem.');
    } finally {
        generateBtn.disabled = false;
    }
}

// --- STUDENTS ---
function openStudentModal(studentId = null) {
    const title = document.getElementById('student-modal-title');
    const form = document.getElementById('student-form');
    
    if (studentId) {
        title.textContent = 'Edytuj Dane Ucznia';
        const s = state.uczniowie.find(u => u.id === studentId);
        if (s) {
            document.getElementById('student-id').value = s.id;
            document.getElementById('student-name').value = s.imie;
            document.getElementById('student-surname').value = s.nazwisko;
            document.getElementById('student-birthdate').value = s.data_urodzenia;
            document.getElementById('student-class').value = s.klasaId;
        }
    } else {
        title.textContent = 'Zarejestruj Nowego Ucznia';
        form.reset();
        document.getElementById('student-id').value = '';
    }
    
    openModal('student-modal');
}

async function submitStudentForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('student-id').value;
    const bodyData = {
        imie: document.getElementById('student-name').value,
        nazwisko: document.getElementById('student-surname').value,
        data_urodzenia: document.getElementById('student-birthdate').value,
        klasaId: parseInt(document.getElementById('student-class').value)
    };

    try {
        let response;
        if (id) {
            response = await fetchWithAuth(`${API_BASE}/uczen/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
        } else {
            response = await fetchWithAuth(`${API_BASE}/uczen`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', id ? 'Zaktualizowano dane ucznia!' : 'Zarejestrowano nowego ucznia!');
            closeModal('student-modal');
            await loadUczniowie();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

async function deleteStudent(id) {
    if (!confirm('Czy na pewno chcesz usunąć tego ucznia? Usunie to wszystkie jego zgłoszenia na wycieczki.')) return;
    
    try {
        const response = await fetchWithAuth(`${API_BASE}/uczen/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('success', 'Usunięto ucznia.');
            await loadUczniowie();
            await loadParticipations();
        } else {
            showAlert('error', 'Nie udało się usunąć ucznia.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

// --- CLASSES ---
function openClassModal(classId = null) {
    const title = document.getElementById('class-modal-title');
    const form = document.getElementById('class-form');
    
    if (classId) {
        title.textContent = 'Edytuj Klasę';
        const k = state.klasy.find(item => item.id === classId);
        if (k) {
            document.getElementById('class-id').value = k.id;
            document.getElementById('class-name').value = k.nazwa;
            document.getElementById('class-profile').value = k.profil || '';
        }
    } else {
        title.textContent = 'Dodaj Nową Klasę';
        form.reset();
        document.getElementById('class-id').value = '';
    }
    
    openModal('class-modal');
}

async function submitClassForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('class-id').value;
    const bodyData = {
        nazwa: document.getElementById('class-name').value,
        profil: document.getElementById('class-profile').value
    };

    try {
        let response;
        if (id) {
            // Edycja (zgodnie z KlasaController, PUT /api/klasy i PUT /api/klasy/{id})
            // Użyjemy PUT /api/klasy/{id} do aktualizacji nazwy
            response = await fetchWithAuth(`${API_BASE}/klasy/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
            
            // Dodatkowo wyślemy aktualizację profilu (PUT /api/klasy) jeśli pierwsza się powiodła
            if (response.ok) {
                await fetchWithAuth(`${API_BASE}/klasy`, {
                    method: 'PUT',
                    body: JSON.stringify({ id: parseInt(id), ...bodyData })
                });
            }
        } else {
            response = await fetchWithAuth(`${API_BASE}/klasy`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', id ? 'Zaktualizowano klasę!' : 'Utworzono nową klasę!');
            closeModal('class-modal');
            await loadKlasy();
            await loadUczniowie(); // Klasy mogą się zmienić na liście uczniów
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

async function deleteClass(id, name, profile) {
    if (!confirm(`Czy na pewno chcesz usunąć klasę ${name}? Uczniowie w tej klasie mogą stracić przypisanie!`)) return;
    
    try {
        // Zgodnie z KlasaController: deleteKlasa bierze KlasaDto w RequestBody!
        const response = await fetchWithAuth(`${API_BASE}/klasy`, {
            method: 'DELETE',
            body: JSON.stringify({ id: id, nazwa: name, profil: profile })
        });

        if (response.ok) {
            showAlert('success', 'Klasa została usunięta.');
            await loadKlasy();
            await loadUczniowie();
        } else {
            showAlert('error', 'Nie udało się usunąć klasy. Upewnij się czy nie ma przypisanych uczniów.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia.');
    }
}

// --- TEACHERS ---
function openTeacherModal(teacherId = null) {
    const title = document.getElementById('teacher-modal-title');
    const form = document.getElementById('teacher-form');
    
    if (teacherId) {
        title.textContent = 'Edytuj Dane Nauczyciela';
        const n = state.nauczyciele.find(item => item.id === teacherId);
        if (n) {
            document.getElementById('teacher-id').value = n.id;
            document.getElementById('teacher-name').value = n.imie;
            document.getElementById('teacher-surname').value = n.nazwisko;
            document.getElementById('teacher-subject').value = n.przedmiot;
            document.getElementById('teacher-phone').value = n.telefon_kontaktowy;
        }
    } else {
        title.textContent = 'Dodaj Nowego Nauczyciela';
        form.reset();
        document.getElementById('teacher-id').value = '';
    }
    
    openModal('teacher-modal');
}

async function submitTeacherForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('teacher-id').value;
    const bodyData = {
        imie: document.getElementById('teacher-name').value,
        nazwisko: document.getElementById('teacher-surname').value,
        przedmiot: document.getElementById('teacher-subject').value,
        telefon_kontaktowy: document.getElementById('teacher-phone').value
    };

    try {
        let response;
        if (id) {
            // Zgodnie z NauczycielController:
            // PUT /api/nauczyciel/{id} aktualizuje imie i nazwisko
            // PUT /api/nauczyciel aktualizuje przedmiot i telefon
            response = await fetchWithAuth(`${API_BASE}/nauczyciel/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
            
            if (response.ok) {
                await fetchWithAuth(`${API_BASE}/nauczyciel`, {
                    method: 'PUT',
                    body: JSON.stringify({ id: parseInt(id), ...bodyData })
                });
            }
        } else {
            response = await fetchWithAuth(`${API_BASE}/nauczyciel`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', id ? 'Zaktualizowano dane nauczyciela!' : 'Dodano nowego nauczyciela!');
            closeModal('teacher-modal');
            await loadNauczyciele();
            await loadOpiekunowie();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

async function deleteTeacher(id, imie, nazwisko, przedmiot, telefon) {
    if (!confirm(`Czy na pewno chcesz usunąć nauczyciela ${imie} ${nazwisko}?`)) return;
    
    try {
        // NauczycielController: deleteNauczyciel oczekuje NauczycielDto w body
        const response = await fetchWithAuth(`${API_BASE}/nauczyciel`, {
            method: 'DELETE',
            body: JSON.stringify({
                id: id,
                imie: imie,
                nazwisko: nazwisko,
                przedmiot: przedmiot,
                telefon_kontaktowy: telefon
            })
        });

        if (response.ok) {
            showAlert('success', 'Nauczyciel został usunięty.');
            await loadNauczyciele();
            await loadOpiekunowie();
        } else {
            showAlert('error', 'Nie udało się usunąć nauczyciela.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia.');
    }
}

// --- GUIDES (OPIEKUNOWIE WYCIECZEK) ---
function openGuideModal(guideId = null) {
    const title = document.getElementById('guide-modal-title');
    const form = document.getElementById('guide-form');
    
    if (guideId) {
        title.textContent = 'Edytuj Przypisanie Opiekuna';
        const g = state.opiekunowie.find(item => item.id === guideId);
        if (g) {
            document.getElementById('guide-id').value = g.id;
            document.getElementById('guide-trip').value = g.wycieczkaId;
            document.getElementById('guide-teacher').value = g.nauczycielId;
            document.getElementById('guide-role').value = g.rola || 'POMOCNIK';
        }
    } else {
        title.textContent = 'Przypisz Opiekuna do Wycieczki';
        form.reset();
        document.getElementById('guide-id').value = '';
    }
    
    openModal('guide-modal');
}

async function submitGuideForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('guide-id').value;
    const bodyData = {
        rola: document.getElementById('guide-role').value,
        wycieczkaId: parseInt(document.getElementById('guide-trip').value),
        nauczycielId: parseInt(document.getElementById('guide-teacher').value)
    };

    try {
        let response;
        if (id) {
            response = await fetchWithAuth(`${API_BASE}/opiekun_wycieczki/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
        } else {
            response = await fetchWithAuth(`${API_BASE}/opiekun_wycieczki`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', 'Zapisano przypisanie opiekuna!');
            closeModal('guide-modal');
            await loadOpiekunowie();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

async function deleteGuide(id) {
    if (!confirm('Czy na pewno chcesz usunąć to przypisanie opiekuna?')) return;
    
    try {
        const response = await fetchWithAuth(`${API_BASE}/opiekun_wycieczki/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('success', 'Usunięto opiekuna z wycieczki.');
            await loadOpiekunowie();
        } else {
            showAlert('error', 'Nie udało się usunąć opiekuna.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia.');
    }
}

// --- ENROLLMENTS & CONSENTS ---
function openEnrollModal() {
    const form = document.getElementById('enroll-form');
    form.reset();
    openModal('enroll-modal');
}

async function submitEnrollForm(event) {
    event.preventDefault();
    
    const uczenId = parseInt(document.getElementById('enroll-student').value);
    const wycieczkaId = parseInt(document.getElementById('enroll-trip').value);
    const czyJedzie = document.getElementById('enroll-is-going').checked;
    const uwagi = document.getElementById('enroll-notes').value;

    const bodyData = {
        uczenId: uczenId,
        wycieczkaId: wycieczkaId,
        czyJedzie: czyJedzie,
        uwagi: uwagi || ''
    };

    try {
        const response = await fetchWithAuth(`${API_BASE}/uczestnictwo`, {
            method: 'POST',
            body: JSON.stringify(bodyData)
        });

        if (response.ok) {
            const newPart = await response.json();
            
            // Opcjonalna zgoda rodzica
            const consentFormType = document.getElementById('enroll-consent-form').value;
            if (consentFormType && czyJedzie) {
                const consentData = {
                    uczestnictwoId: newPart.id,
                    forma: consentFormType,
                    dataPodpisania: new Date().toISOString().split('T')[0],
                    czyDostarczona: true
                };
                
                await fetchWithAuth(`${API_BASE}/zgoda_rodzica`, {
                    method: 'POST',
                    body: JSON.stringify(consentData)
                });
            }

            showAlert('success', 'Uczeń został zapisany na wycieczkę!');
            closeModal('enroll-modal');
            
            await loadParticipations();
            await loadConsents();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd podczas zapisu.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

async function deleteParticipation(id) {
    if (!confirm('Czy na pewno chcesz usunąć to zgłoszenie? Spowoduje to również usunięcie zgody rodzica.')) return;
    
    try {
        const response = await fetchWithAuth(`${API_BASE}/uczestnictwo/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('success', 'Wycofano zapis na wycieczkę.');
            await loadParticipations();
            await loadConsents();
        } else {
            showAlert('error', 'Nie udało się usunąć zapisu.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia.');
    }
}

// Osobne zarządzanie zgodą rodzica
function openConsentModal(participationId, consentId = null) {
    const title = document.getElementById('consent-modal-title');
    const form = document.getElementById('consent-form');
    form.reset();
    
    document.getElementById('consent-participation-id').value = participationId;
    document.getElementById('consent-id').value = consentId || '';
    
    const part = state.uczestnictwa.find(p => p.id === participationId);
    if (part) {
        const student = state.uczniowie.find(u => u.id === part.uczenId);
        const wycieczka = state.wycieczki.find(w => w.id === part.wycieczkaId);
        
        const studentName = student ? `${student.imie} ${student.nazwisko}` : `Uczeń (ID: ${part.uczenId})`;
        const tripName = wycieczka ? wycieczka.nazwa : `Wycieczka (ID: ${part.wycieczkaId})`;
        
        document.getElementById('consent-info-student').value = `${studentName} ➔ ${tripName}`;
    }
    
    if (consentId) {
        title.textContent = 'Edytuj Zgodę Rodzica';
        const consent = state.zgody.find(z => z.id === consentId);
        if (consent) {
            document.getElementById('consent-form-type').value = consent.forma || 'ELEKTRONICZNA';
            document.getElementById('consent-date').value = consent.dataPodpisania || '';
            document.getElementById('consent-delivered').checked = consent.czyDostarczona;
        }
    } else {
        title.textContent = 'Dodaj Zgodę Rodzica';
        document.getElementById('consent-date').value = new Date().toISOString().split('T')[0];
        document.getElementById('consent-delivered').checked = true;
    }
    
    openModal('consent-modal');
}

async function submitConsentForm(event) {
    event.preventDefault();
    
    const id = document.getElementById('consent-id').value;
    const partId = parseInt(document.getElementById('consent-participation-id').value);
    
    const bodyData = {
        uczestnictwoId: partId,
        forma: document.getElementById('consent-form-type').value,
        dataPodpisania: document.getElementById('consent-date').value,
        czyDostarczona: document.getElementById('consent-delivered').checked
    };

    try {
        let response;
        if (id) {
            response = await fetchWithAuth(`${API_BASE}/zgoda_rodzica/${id}`, {
                method: 'PUT',
                body: JSON.stringify(bodyData)
            });
        } else {
            response = await fetchWithAuth(`${API_BASE}/zgoda_rodzica`, {
                method: 'POST',
                body: JSON.stringify(bodyData)
            });
        }

        if (response.ok) {
            showAlert('success', 'Zapisano status zgody rodzicielskiej.');
            closeModal('consent-modal');
            await loadConsents();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd zapisu zgody.');
        }
    } catch (e) {
        showAlert('error', 'Błąd komunikacji.');
    }
}

// Pobieranie i podgląd PDF zgody rodzica bezpośrednio w przeglądarce
async function downloadConsentPdf(consentId) {
    try {
        const response = await fetchWithAuth(`${API_BASE}/zgoda_rodzica/${consentId}/pdf`);
        
        if (!response.ok) {
            throw new Error('Nie udało się wygenerować dokumentu PDF');
        }

        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);
        
        // Otwieramy podgląd PDF w nowej zakładce
        const newWindow = window.open();
        if (newWindow) {
            newWindow.location.href = blobUrl;
        } else {
            // Jeśli przeglądarka zablokowała popup, wymuszamy pobranie pliku
            const a = document.createElement('a');
            a.href = blobUrl;
            a.download = `zgoda_rodzica_${consentId}.pdf`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        }
    } catch (error) {
        console.error('Błąd podczas generowania pliku PDF:', error);
        showAlert('error', 'Wystąpił błąd podczas pobierania dokumentu PDF.');
    }
}

// Funkcja obsługująca pobieranie PDF z uczestnikami wycieczki
async function downloadTripPdf() {
    if (!selectedTripForDetailsId) return;
    try {
        const response = await fetchWithAuth(`${API_BASE}/wycieczka/${selectedTripForDetailsId}/pdf`);
        
        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.message || 'Nie udało się pobrać pliku PDF');
        }

        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = `uczestnicy_wycieczki_${selectedTripForDetailsId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(blobUrl);
    } catch (error) {
        console.error('Błąd podczas pobierania pliku PDF wycieczki:', error);
        showAlert('error', error.message || 'Wystąpił błąd podczas pobierania dokumentu PDF.');
    }
}

// Funkcja obsługująca zapis na wycieczkę bezpośrednio z karty wycieczki
function enrollInTripFromCard(tripId) {
    switchTab('zapis-wycieczka-section');
    const tripSelect = document.getElementById('student-direct-enroll-trip');
    if (tripSelect) {
        tripSelect.value = tripId;
    }
}

// Obsługa wysłania formularza zapisu bezpośrednio przez uchodźcę/ucznia
async function submitStudentDirectEnrollForm(event) {
    event.preventDefault();
    
    const uczenId = parseInt(document.getElementById('student-direct-enroll-uczen').value);
    const wycieczkaId = parseInt(document.getElementById('student-direct-enroll-trip').value);
    const czyJedzie = document.getElementById('student-direct-enroll-is-going').checked;
    const uwagi = document.getElementById('student-direct-enroll-notes').value;

    const bodyData = {
        uczenId: uczenId,
        wycieczkaId: wycieczkaId,
        czyJedzie: czyJedzie,
        uwagi: uwagi || ''
    };

    try {
        const response = await fetchWithAuth(`${API_BASE}/uczestnictwo`, {
            method: 'POST',
            body: JSON.stringify(bodyData)
        });

        if (response.ok) {
            const newPart = await response.json();
            
            // Rejestracja zgody rodzica
            const consentFormType = document.getElementById('student-direct-enroll-consent').value;
            if (consentFormType && czyJedzie) {
                const consentData = {
                    uczestnictwoId: newPart.id,
                    forma: consentFormType,
                    dataPodpisania: new Date().toISOString().split('T')[0],
                    czyDostarczona: true
                };
                
                await fetchWithAuth(`${API_BASE}/zgoda_rodzica`, {
                    method: 'POST',
                    body: JSON.stringify(consentData)
                });
            }

            showAlert('success', 'Zgłoszenie i zgoda rodzicielska zostały pomyślnie przesłane!');
            
            // Wyczyszczenie formularza i przełączenie widoku
            document.getElementById('student-direct-enroll-form').reset();
            switchTab('wycieczki-tab-section');
            
            await loadInitialData();
        } else {
            const err = await response.json().catch(() => ({}));
            showAlert('error', err.message || 'Wystąpił błąd podczas rejestracji zapisu.');
        }
    } catch (e) {
        showAlert('error', 'Błąd połączenia z serwerem.');
    }
}

// ================= POMOCNICZE UTILITY =================

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Wyświetlanie powiadomień alert
function showAlert(type, message) {
    const container = document.getElementById('alert-container');
    if (!container) return;
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    
    // Ikona powiadomienia w zależności od typu
    let iconSvg = '';
    if (type === 'success') {
        iconSvg = `<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
    } else if (type === 'error') {
        iconSvg = `<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
    } else {
        iconSvg = `<svg class="alert-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`;
    }

    alertDiv.innerHTML = `
        ${iconSvg}
        <div class="alert-content">${message}</div>
    `;

    container.appendChild(alertDiv);

    // Automatyczne zniknięcie po 4.5 sekundy
    setTimeout(() => {
        alertDiv.style.transition = 'all 0.5s ease-out';
        alertDiv.style.opacity = '0';
        alertDiv.style.transform = 'translateX(40px)';
        setTimeout(() => alertDiv.remove(), 500);
    }, 4500);
}