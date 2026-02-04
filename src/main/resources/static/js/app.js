const API_BASE = '/api';


const state = {
    wycieczki: [],
    klasy: [],
    uczniowie: [],
    currentFilter: 'wszystkie'
};


document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    loadInitialData();
});


function initNavigation() {
    const navTabs = document.querySelectorAll('.nav-tab');
    navTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const target = tab.dataset.section;
            switchSection(target);


            navTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
        });
    });
}

function switchSection(sectionId) {
    const sections = document.querySelectorAll('.section');
    sections.forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId).classList.add('active');
}


async function loadInitialData() {
    await Promise.all([
        loadWycieczki(),
        loadKlasy(),
        loadUczniowie()
    ]);
}


async function loadWycieczki() {
    try {
        const response = await fetch(`${API_BASE}/wycieczka`);
        if (!response.ok) throw new Error('Nie udało się załadować wycieczek');

        state.wycieczki = await response.json();
        renderWycieczki(state.wycieczki);
        populateWycieczkaSelect();
    } catch (error) {
        console.error('Error loading wycieczki:', error);
        showAlert('error', 'Nie udało się załadować wycieczek. Sprawdź czy serwer działa.');
    }
}


function renderWycieczki(wycieczki) {
    const container = document.getElementById('trips-container');

    if (wycieczki.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>Brak dostępnych wycieczek</h3>
                <p>Obecnie nie ma żadnych wycieczek w systemie</p>
            </div>
        `;
        return;
    }

    container.innerHTML = wycieczki.map(wycieczka => `
        <div class="trip-card">
            <h3>${wycieczka.nazwa}</h3>
            <div class="trip-detail">
                <svg fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
                </svg>
                <span>${wycieczka.miejsce_docelowe}</span>
            </div>
            <div class="trip-detail">
                <svg fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/>
                </svg>
                <span>${formatDate(wycieczka.data_rozpoczecia)} - ${formatDate(wycieczka.data_zakonczenia)}</span>
            </div>
            <div class="trip-cost">${wycieczka.koszt_na_osobe} zł / osoba</div>
            <span class="trip-status status-${wycieczka.status?.toLowerCase() || 'planowana'}">${wycieczka.status || 'Planowana'}</span>
        </div>
    `).join('');
}


function filterWycieczki(status) {
    state.currentFilter = status;

    const filterBtns = document.querySelectorAll('.filter-btn');
    filterBtns.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.filter === status);
    });

    const filtered = status === 'wszystkie'
        ? state.wycieczki
        // ZMIANA PONIŻEJ: Dodano bezpieczne sprawdzanie (w.status || '')
        : state.wycieczki.filter(w => (w.status || '').toLowerCase() === status.toLowerCase());

    renderWycieczki(filtered);
}


async function loadKlasy() {
    try {
        // BYŁO: const response = await fetch(`${API_BASE}/klasa`);
        // MA BYĆ (dodaj "y" na końcu):
        const response = await fetch(`${API_BASE}/klasy`);

        if (!response.ok) throw new Error('Nie udało się załadować klas');

        state.klasy = await response.json();
        populateKlasaSelect();
    } catch (error) {
        console.error('Error loading klasy:', error);
    }
}


async function loadUczniowie() {
    try {
        const response = await fetch(`${API_BASE}/uczen`);
        if (!response.ok) throw new Error('Nie udało się załadować uczniów');

        state.uczniowie = await response.json();
        populateUczenSelect();
    } catch (error) {
        console.error('Error loading uczniowie:', error);
    }
}


function populateKlasaSelect() {
    const select = document.getElementById('uczen-klasa');
    if (!select) return;

    select.innerHTML = '<option value="">Wybierz klasę</option>' +
        state.klasy.map(klasa =>
            `<option value="${klasa.id}">${klasa.nazwa || `Klasa ${klasa.id}`}</option>`
        ).join('');
}

function populateUczenSelect() {
    const select = document.getElementById('uczestnictwo-uczen');
    if (!select) return;

    select.innerHTML = '<option value="">Wybierz ucznia</option>' +
        state.uczniowie.map(uczen =>
            `<option value="${uczen.id}">${uczen.imie} ${uczen.nazwisko}</option>`
        ).join('');
}

function populateWycieczkaSelect() {
    const select = document.getElementById('uczestnictwo-wycieczka');
    if (!select) return;

    select.innerHTML = '<option value="">Wybierz wycieczkę</option>' +
        state.wycieczki.map(wycieczka =>
            `<option value="${wycieczka.id}">${wycieczka.nazwa}</option>`
        ).join('');
}


async function registerStudent(event) {
    event.preventDefault();

    const formData = {
        imie: document.getElementById('uczen-imie').value,
        nazwisko: document.getElementById('uczen-nazwisko').value,
        data_urodzenia: document.getElementById('uczen-data-urodzenia').value,
        klasaId: parseInt(document.getElementById('uczen-klasa').value) // <--- POPRAWIONE
    };

    try {
        const response = await fetch(`${API_BASE}/uczen`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) throw new Error('Nie udało się zarejestrować ucznia');

        const newStudent = await response.json();
        state.uczniowie.push(newStudent);
        populateUczenSelect();

        showAlert('success', `Uczeń ${formData.imie} ${formData.nazwisko} został pomyślnie zarejestrowany!`);
        event.target.reset();


        await loadUczniowie();

    } catch (error) {
        console.error('Error registering student:', error);
        showAlert('error', 'Wystąpił błąd podczas rejestracji ucznia. Spróbuj ponownie.');
    }
}


async function registerForTrip(event) {
    event.preventDefault();

    const uczenId = parseInt(document.getElementById('uczestnictwo-uczen').value);
    const wycieczkaId = parseInt(document.getElementById('uczestnictwo-wycieczka').value);
    const czyJedzie = document.getElementById('uczestnictwo-czy-jedzie').checked;
    const uwagi = document.getElementById('uczestnictwo-uwagi').value;

    if (!uczenId || !wycieczkaId) {
        showAlert('error', 'Proszę wybrać ucznia i wycieczkę');
        return;
    }


    const uczestnictwoData = {
        uczenId: uczenId,           // Było: id_uczen
        wycieczkaId: wycieczkaId,   // Było: id_wycieczki
        // data_zapisania - usuń to, backend ustawia datę sam w Service
        czyJedzie: czyJedzie,       // Było: czy_jedzie
        uwagi: uwagi || ''
    };

    try {
        const response = await fetch(`${API_BASE}/uczestnictwo`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(uczestnictwoData)
        });

        if (!response.ok) throw new Error('Nie udało się zapisać na wycieczkę');

        const uczestnictwo = await response.json();


        const formaZgody = document.getElementById('zgoda-forma').value;
        if (formaZgody && czyJedzie) {
            await submitParentalConsent(uczestnictwo.id, formaZgody);
        }

        const student = state.uczniowie.find(u => u.id === uczenId);
        const trip = state.wycieczki.find(w => w.id === wycieczkaId);

        showAlert('success', `Zapisano ${student.imie} ${student.nazwisko} na wycieczkę "${trip.nazwa}"!`);
        event.target.reset();

    } catch (error) {
        console.error('Error registering for trip:', error);
        showAlert('error', 'Wystąpił błąd podczas zapisu na wycieczkę. Spróbuj ponownie.');
    }
}


async function submitParentalConsent(uczestnictwoId, forma) {
    const zgodaData = {

        uczestnictwoId: uczestnictwoId, // POPRAWIONE

        forma: forma,

        // BYŁO: data_wystawienia: ...
        dataPodpisania: new Date().toISOString().split('T')[0], // POPRAWIONE (zgodnie z DTO)

        // TEGO BRAKOWAŁO:
        czyDostarczona: true
    };

    try {
        const response = await fetch(`${API_BASE}/zgoda_rodzica`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(zgodaData)
        });

        if (!response.ok) throw new Error('Nie udało się zapisać zgody rodzica');

        return await response.json();

    } catch (error) {
        console.error('Error submitting parental consent:', error);
        showAlert('error', 'Nie udało się zapisać zgody rodzica');
    }
}


function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('pl-PL', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function showAlert(type, message) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-error';
    const alertHTML = `
        <div class="alert ${alertClass}">
            ${message}
        </div>
    `;


    const activeSection = document.querySelector('.section.active');
    if (activeSection) {

        activeSection.insertAdjacentHTML('afterbegin', alertHTML);


        setTimeout(() => {
            const alert = activeSection.querySelector('.alert');
            if (alert) {
                alert.style.animation = 'fadeOut 0.3s ease-out';
                setTimeout(() => alert.remove(), 300);
            }
        }, 5000);
    }
}


const styleSheet = document.createElement('style');
styleSheet.textContent = `
    @keyframes fadeOut {
        from { opacity: 1; transform: translateY(0); }
        to { opacity: 0; transform: translateY(-10px); }
    }
`;
document.head.appendChild(styleSheet);

