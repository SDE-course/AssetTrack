import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../../styles/auth.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const Register = () => {
    const navigate = useNavigate();

    const [form, setForm]         = useState({ name: '', email: '', password: '' });
    const [errors, setErrors]     = useState({});
    const [apiError, setApiError] = useState('');
    const [loading, setLoading]   = useState(false);
    const [success, setSuccess]   = useState(false);

    // ── Validation ────────────────────────────────────────────────
    const validate = () => {
        const errs = {};
        if (!form.name || form.name.trim().length < 2)
            errs.name = 'Name must be at least 2 characters';
        if (!form.email)
            errs.email = 'Email is required';
        else if (!/\S+@\S+\.\S+/.test(form.email))
            errs.email = 'Enter a valid email';
        if (!form.password)
            errs.password = 'Password is required';
        else if (form.password.length < 8)
            errs.password = 'Password must be at least 8 characters';
        return errs;
    };

    // ── Handlers ──────────────────────────────────────────────────
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
        if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
        setApiError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const errs = validate();
        if (Object.keys(errs).length > 0) { setErrors(errs); return; }

        setLoading(true);
        setApiError('');

        try {
            const res = await fetch(`${API_BASE}/api/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: form.name.trim(),
                    email: form.email,
                    password: form.password,
                }),
            });

            const data = await res.json();

            if (!res.ok) {
                setApiError(data.message || 'Registration failed. Please try again.');
                return;
            }

            // Store JWT immediately — user is logged in right after sign-up
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify({
                name: data.name,
                email: data.email,
                role: data.role,
            }));

            setSuccess(true);

            // Short delay so the user sees the success state, then redirect
            setTimeout(() => navigate('/dashboard'), 1800);

        } catch {
            setApiError('Cannot reach the server. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    // ── Success screen ────────────────────────────────────────────
    if (success) {
        return (
            <div className="auth-page">
                <div className="auth-card login-card" style={{ textAlign: 'center' }}>
                    <div className="auth-success-icon">🎉</div>
                    <h1 className="auth-heading">You're in!</h1>
                    <p className="auth-subheading">Account created. Redirecting you now…</p>
                </div>
            </div>
        );
    }

    // ── Main form ─────────────────────────────────────────────────
    return (
        <div className="auth-page">
            <div className="auth-card register-card">

                {/* Left — form */}
                <div className="register-form-section">
                    <div className="auth-brand">
                        <div className="auth-brand-icon">💼</div>
                        <span className="auth-brand-name">AssetTrack</span>
                    </div>

                    <h1 className="auth-heading">Create account</h1>
                    <p className="auth-subheading">Start tracking assets in minutes</p>

                    {apiError && (
                        <div className="auth-alert error">{apiError}</div>
                    )}

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="input-group">
                            <label htmlFor="name">Full Name</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                placeholder="Jane Smith"
                                value={form.name}
                                onChange={handleChange}
                                className={errors.name ? 'input-error' : ''}
                                autoComplete="name"
                            />
                            {errors.name && <p className="field-error">{errors.name}</p>}
                        </div>

                        <div className="input-group">
                            <label htmlFor="email">Work Email</label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                placeholder="jane@company.com"
                                value={form.email}
                                onChange={handleChange}
                                className={errors.email ? 'input-error' : ''}
                                autoComplete="email"
                            />
                            {errors.email && <p className="field-error">{errors.email}</p>}
                        </div>

                        <div className="input-group">
                            <label htmlFor="password">Password</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                placeholder="Min. 8 characters"
                                value={form.password}
                                onChange={handleChange}
                                className={errors.password ? 'input-error' : ''}
                                autoComplete="new-password"
                            />
                            {errors.password && <p className="field-error">{errors.password}</p>}
                        </div>

                        <button type="submit" className="auth-button" disabled={loading}>
                            {loading && <span className="btn-spinner" />}
                            {loading ? 'Creating account…' : 'Create Account'}
                        </button>
                    </form>

                    <p className="auth-footer">
                        Already have an account?{' '}
                        <Link to="/login" className="auth-link">Sign in</Link>
                    </p>
                </div>

                {/* Right — decorative panel */}
                <div className="register-panel">
                    <div className="panel-icon">🖥️</div>
                    <p className="panel-title">Track every asset, always</p>
                    <p className="panel-subtitle">
                        Real-time visibility into laptops, monitors, and accessories across your entire team.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Register;